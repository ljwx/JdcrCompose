package com.jdcr.jdcrpullrefresh

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** The lifecycle state exposed to a custom pull-to-refresh header. */
enum class PullRefreshStatus {
    Idle,
    Pulling,
    ReadyToRefresh,
    Refreshing,
    RefreshComplete,
    RefreshFailed,
}

/** The result shown briefly after the refresh callback completes. */
enum class PullRefreshResult {
    Success,
    Failure,
}

/** Completion controls available inside the suspend refresh callback. */
class PullRefreshCallbackScope internal constructor(
    private val onFinish: (PullRefreshResult) -> Unit,
) {
    /** Completes the current refresh with an explicit result. */
    fun finish(result: PullRefreshResult = PullRefreshResult.Success) {
        onFinish(result)
    }
}

/**
 * State for [JdcrPullRefresh].
 *
 * A state is intentionally independent from a particular scrollable. Attach it to a
 * [JdcrPullRefresh] once, and use [refresh] or [finishRefresh] when a programmatic flow needs
 * to control it.
 */
@Stable
class PullRefreshState internal constructor(
    private val onRefresh: suspend PullRefreshCallbackScope.() -> Unit,
    private val onRefreshError: (Throwable) -> Unit,
    private val scope: CoroutineScope,
    internal val triggerDistancePx: Float,
    internal val maxPullDistancePx: Float,
    internal val dragRate: Float,
    val triggerDistance: Dp,
    val maxPullDistance: Dp,
    val contentScrollableWhileRefreshing: Boolean,
    private val finishDelayMillis: Long,
    private val reboundSpec: AnimationSpec<Float>,
) {
    private val rawPullAnimation = Animatable(0f)
    private var settleJob: Job? = null
    private var refreshJob: Job? = null
    private var finishJob: Job? = null
    private var refreshGeneration = 0L

    @Volatile
    private var requestedCompletionGeneration = -1L

    private var rawPullDistancePx = 0f
    private var containerHeightPx = 0f
    private var offsetPx by mutableFloatStateOf(0f)

    /** Current lifecycle state. */
    var status: PullRefreshStatus by mutableStateOf(PullRefreshStatus.Idle)
        private set

    /** Whether the refresh callback is currently running. */
    val isRefreshing: Boolean
        get() = status == PullRefreshStatus.Refreshing

    /** Current visible offset of the content and Header, in pixels. */
    internal val contentOffsetPx: Float
        get() = offsetPx

    /** Current visible pull distance, in pixels. */
    internal val indicatorOffsetPx: Float
        get() = offsetPx

    /** Pull distance ratio. A value of 1 reaches the threshold; over-pull can exceed 1. */
    val progress: Float
        get() = if (triggerDistancePx == 0f) 0f else offsetPx / triggerDistancePx

    /** Pull distance in dp for custom headers. */
    internal fun pullDistance(density: Density): Dp = with(density) { offsetPx.toDp() }

    /**
     * Starts a refresh from code. The same callback and completion behavior as a gesture refresh
     * are used. Calling this while refreshing is a no-op.
     */
    fun refresh() {
        scope.launch { startRefresh() }
    }

    /**
     * Completes a refresh started by this state. Normally the suspend callback is enough and the
     * state completes automatically; this method is useful when an integration owns completion
     * itself or wants to report a failure explicitly.
     */
    fun finishRefresh(result: PullRefreshResult = PullRefreshResult.Success) {
        requestFinish(result, refreshGeneration)
    }

    internal fun consumePreScroll(deltaY: Float): Float {
        if (isRefreshing && !contentScrollableWhileRefreshing) return deltaY
        return consumeUpward(deltaY)
    }

    private fun consumeUpward(deltaY: Float): Float {
        if (deltaY >= 0f || rawPullDistancePx <= 0f) return 0f
        if (!isRefreshing &&
            status != PullRefreshStatus.Pulling &&
            status != PullRefreshStatus.ReadyToRefresh
        ) return 0f

        settleJob?.cancel()
        val maxRawConsumption = -rawPullDistancePx
        val consumed = max(deltaY, maxRawConsumption)
        setRawPullDistance(rawPullDistancePx + consumed)
        updatePullStatus()
        return consumed
    }

    internal fun consumeDownward(deltaY: Float): Float {
        if (deltaY <= 0f || status == PullRefreshStatus.RefreshComplete ||
            status == PullRefreshStatus.RefreshFailed
        ) return 0f
        if (isRefreshing && !contentScrollableWhileRefreshing) return deltaY

        settleJob?.cancel()
        setRawPullDistance(rawPullDistancePx + deltaY)
        if (!isRefreshing) updatePullStatus()
        return deltaY
    }

    internal fun release(velocityY: Float): Boolean {
        if (isRefreshing) {
            if (!contentScrollableWhileRefreshing) return true
            if (velocityY < 0f && indicatorOffsetPx > 0f) {
                settleJob?.cancel()
                settleJob = scope.launch { animateOffsetTo(0f) }
                // Let the child keep the upward velocity while the Header closes.
                return false
            }

            if (indicatorOffsetPx <= 0f ||
                abs(indicatorOffsetPx - triggerDistancePx) < 0.5f
            ) return false
            settleJob?.cancel()
            settleJob = scope.launch { animateOffsetTo(triggerDistancePx) }
            return true
        }

        if (status != PullRefreshStatus.Pulling && status != PullRefreshStatus.ReadyToRefresh) {
            return false
        }

        if (status == PullRefreshStatus.ReadyToRefresh) {
            scope.launch { startRefresh() }
        } else {
            settleJob?.cancel()
            settleJob = scope.launch {
                animateOffsetTo(0f)
                if (status == PullRefreshStatus.Pulling) {
                    status = PullRefreshStatus.Idle
                }
            }
        }
        return true
    }

    private suspend fun startRefresh() {
        if (isRefreshing) return

        settleJob?.cancel()
        finishJob?.cancel()
        refreshJob?.cancel()
        status = PullRefreshStatus.Refreshing
        val generation = ++refreshGeneration

        settleJob = scope.launch {
            animateOffsetTo(triggerDistancePx)
        }
        refreshJob = scope.launch {
            val callbackScope = PullRefreshCallbackScope { result ->
                requestFinish(result, generation)
            }
            try {
                if (generation != refreshGeneration || status != PullRefreshStatus.Refreshing) {
                    return@launch
                }
                callbackScope.onRefresh()
                if (requestedCompletionGeneration != generation &&
                    generation == refreshGeneration &&
                    status == PullRefreshStatus.Refreshing
                ) {
                    finishRefreshInternal(PullRefreshResult.Success, generation)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (requestedCompletionGeneration != generation &&
                    generation == refreshGeneration &&
                    status == PullRefreshStatus.Refreshing
                ) {
                    finishRefreshInternal(PullRefreshResult.Failure, generation)
                    onRefreshError(error)
                }
            }
        }
    }

    private fun requestFinish(result: PullRefreshResult, generation: Long) {
        requestedCompletionGeneration = generation
        scope.launch { finishRefreshInternal(result, generation) }
    }

    private fun finishRefreshInternal(result: PullRefreshResult, generation: Long) {
        if (generation != refreshGeneration || !isRefreshing) return

        status = when (result) {
            PullRefreshResult.Success -> PullRefreshStatus.RefreshComplete
            PullRefreshResult.Failure -> PullRefreshStatus.RefreshFailed
        }

        settleJob?.cancel()
        finishJob?.cancel()
        finishJob = scope.launch {
            if (finishDelayMillis > 0) delay(finishDelayMillis)
            animateOffsetTo(0f)
            if (status == PullRefreshStatus.RefreshComplete ||
                status == PullRefreshStatus.RefreshFailed
            ) {
                status = PullRefreshStatus.Idle
            }
        }
    }

    private suspend fun animateOffsetTo(target: Float) {
        val targetRawDistance = rawDistanceForOffset(target)
        rawPullAnimation.snapTo(rawPullDistancePx)
        rawPullAnimation.animateTo(targetRawDistance, reboundSpec) {
            setRawPullDistance(value)
        }
        setRawPullDistance(targetRawDistance)
        setOffset(target)
    }

    private fun setOffset(value: Float) {
        offsetPx = value.coerceIn(0f, maxPullDistancePx)
    }

    private fun setRawPullDistance(value: Float) {
        rawPullDistancePx = value.coerceAtLeast(0f)
        setOffset(
            calculatePullOffset(
                rawDistancePx = rawPullDistancePx,
                dragRate = dragRate,
                triggerDistancePx = triggerDistancePx,
                maxPullDistancePx = maxPullDistancePx,
                resistanceHeightPx = max(containerHeightPx, maxPullDistancePx),
            ),
        )
    }

    private fun rawDistanceForOffset(targetOffsetPx: Float): Float {
        if (targetOffsetPx <= 0f) return 0f

        var low = 0f
        var high = max(targetOffsetPx / dragRate, 1f)
        while (calculatePullOffset(
                rawDistancePx = high,
                dragRate = dragRate,
                triggerDistancePx = triggerDistancePx,
                maxPullDistancePx = maxPullDistancePx,
                resistanceHeightPx = max(containerHeightPx, maxPullDistancePx),
            ) < targetOffsetPx && high < maxPullDistancePx * 1_000f
        ) {
            high *= 2f
        }
        repeat(24) {
            val middle = (low + high) / 2f
            val offset = calculatePullOffset(
                rawDistancePx = middle,
                dragRate = dragRate,
                triggerDistancePx = triggerDistancePx,
                maxPullDistancePx = maxPullDistancePx,
                resistanceHeightPx = max(containerHeightPx, maxPullDistancePx),
            )
            if (offset < targetOffsetPx) low = middle else high = middle
        }
        return high
    }

    internal fun updateContainerHeight(heightPx: Float) {
        containerHeightPx = heightPx.coerceAtLeast(0f)
    }

    internal fun dispose() {
        settleJob?.cancel()
        refreshJob?.cancel()
        finishJob?.cancel()
    }

    private fun updatePullStatus() {
        if (isRefreshing) return
        status = when {
            offsetPx <= 0f -> PullRefreshStatus.Idle
            offsetPx >= triggerDistancePx -> PullRefreshStatus.ReadyToRefresh
            else -> PullRefreshStatus.Pulling
        }
    }
}

/**
 * Creates state remembered across recompositions.
 *
 * `onRefresh` is suspend so callers can keep the real request in one place. Once it returns, the
 * Header enters the complete state and then rebounds closed; an exception is shown as failure and
 * delivered to `onRefreshError`. By default, content remains scrollable while refreshing and an
 * upward gesture can close the Header without cancelling the refresh task.
 */
@Composable
fun rememberPullRefreshState(
    onRefresh: suspend PullRefreshCallbackScope.() -> Unit,
    onRefreshError: (Throwable) -> Unit = {},
    triggerDistance: Dp = 96.dp,
    maxPullDistance: Dp = triggerDistance * 2.5f,
    dragRate: Float = 0.70f,
    contentScrollableWhileRefreshing: Boolean = true,
    finishDelayMillis: Long = 300L,
    reboundSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    ),
): PullRefreshState {
    require(triggerDistance > 0.dp) { "triggerDistance must be greater than zero" }
    require(maxPullDistance > triggerDistance) {
        "maxPullDistance must be greater than triggerDistance"
    }
    require(dragRate > 0f) { "dragRate must be greater than zero" }
    require(finishDelayMillis >= 0L) { "finishDelayMillis must not be negative" }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val latestOnRefresh by androidx.compose.runtime.rememberUpdatedState(onRefresh)
    val latestOnRefreshError by androidx.compose.runtime.rememberUpdatedState(onRefreshError)

    val state = androidx.compose.runtime.remember(
        density,
        scope,
        triggerDistance,
        maxPullDistance,
        dragRate,
        contentScrollableWhileRefreshing,
        finishDelayMillis,
        reboundSpec,
    ) {
        PullRefreshState(
            onRefresh = { latestOnRefresh(this) },
            onRefreshError = { latestOnRefreshError(it) },
            scope = scope,
            triggerDistancePx = with(density) { triggerDistance.toPx() },
            maxPullDistancePx = with(density) { maxPullDistance.toPx() },
            dragRate = dragRate,
            triggerDistance = triggerDistance,
            maxPullDistance = maxPullDistance,
            contentScrollableWhileRefreshing = contentScrollableWhileRefreshing,
            finishDelayMillis = finishDelayMillis,
            reboundSpec = reboundSpec,
        )
    }
    androidx.compose.runtime.DisposableEffect(state) {
        onDispose(state::dispose)
    }
    return state
}

internal fun calculatePullOffset(
    rawDistancePx: Float,
    dragRate: Float,
    triggerDistancePx: Float,
    maxPullDistancePx: Float,
    resistanceHeightPx: Float,
): Float {
    if (rawDistancePx <= 0f) return 0f
    val dampedDistance = rawDistancePx * dragRate
    if (dampedDistance <= triggerDistancePx) return dampedDistance

    val overPullInput = dampedDistance - triggerDistancePx
    val maxOverPullDistance = maxPullDistancePx - triggerDistancePx
    val height = resistanceHeightPx.coerceAtLeast(1f)
    val resistedOverPull = maxOverPullDistance *
        (1.0 - 100.0.pow((-overPullInput / height).toDouble())).toFloat()
    return triggerDistancePx + min(resistedOverPull, overPullInput)
}
