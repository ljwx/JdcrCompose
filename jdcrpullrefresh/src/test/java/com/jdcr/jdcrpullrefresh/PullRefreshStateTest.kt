package com.jdcr.jdcrpullrefresh

import androidx.compose.animation.core.spring
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.CoroutineContext

class PullRefreshStateTest {
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Unconfined + ImmediateFrameClock(),
    )

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun downwardPullUsesDragRateAndEntersReadyStateAtThreshold() {
        val state = newState()

        assertEquals(200f, state.consumeDownward(200f), 0.001f)
        assertEquals(PullRefreshStatus.ReadyToRefresh, state.status)
        assertEquals(1f, state.progress, 0.001f)
    }

    @Test
    fun configuredDragRateControlsPreThresholdResponsiveness() {
        val state = newState(dragRate = 0.65f)

        state.consumeDownward(100f)

        assertEquals(PullRefreshStatus.Pulling, state.status)
        assertEquals(0.65f, state.progress, 0.001f)
    }

    @Test
    fun pullIsCappedAtMaximumDistance() {
        val state = newState()

        assertEquals(600f, state.consumeDownward(600f), 0.001f)
        assertTrue(state.progress in 1.99f..2f)
        assertEquals(50f, state.consumeDownward(50f), 0.001f)
        assertTrue(state.progress <= 2f)
    }

    @Test
    fun upwardDragFirstClosesHeaderAndReturnsOnlyTheRemainder() {
        val state = newState()
        state.consumeDownward(160f) // 对应 80 像素的可见下拉距离

        assertEquals(-160f, state.consumePreScroll(-200f), 0.001f)
        assertEquals(PullRefreshStatus.Idle, state.status)
        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun resistanceStartsOnlyAfterRefreshThreshold() {
        val state = newState()
        state.updateContainerHeight(1_000f)

        state.consumeDownward(200f)
        assertEquals(1f, state.progress, 0.001f)

        state.consumeDownward(200f)

        assertTrue(state.progress in 1.36f..1.38f)
    }

    @Test
    fun refreshingHeaderCanCollapseWhileRefreshContinues() {
        val refreshGate = CompletableDeferred<Unit>()
        val state = newState(onRefresh = { refreshGate.await() })
        state.consumeDownward(200f)

        state.refresh()
        val consumed = state.consumePreScroll(-80f)

        assertEquals(PullRefreshStatus.Refreshing, state.status)
        assertEquals(-80f, consumed, 0.001f)
        assertTrue(state.progress < 1f)
        refreshGate.complete(Unit)
    }

    @Test
    fun refreshingUpwardFlingIsPassedToContentWhileHeaderCloses() {
        val refreshGate = CompletableDeferred<Unit>()
        val state = newState(onRefresh = { refreshGate.await() })
        state.consumeDownward(200f)
        state.refresh()

        val consumed = state.release(velocityY = -1_000f)

        assertEquals(false, consumed)
        assertEquals(PullRefreshStatus.Refreshing, state.status)
        assertEquals(0f, state.progress, 0.001f)
        refreshGate.complete(Unit)
    }

    @Test
    fun secondPullDuringRefreshOnlyRevealsTheCurrentHeader() {
        val refreshGate = CompletableDeferred<Unit>()
        var refreshCount = 0
        val state = newState(
            onRefresh = {
                refreshCount++
                refreshGate.await()
            },
        )
        state.consumeDownward(200f)
        state.refresh()
        state.consumePreScroll(-200f)

        state.consumeDownward(100f)
        val releaseConsumed = state.release(velocityY = 0f)

        assertTrue(releaseConsumed)
        assertEquals(1, refreshCount)
        assertEquals(PullRefreshStatus.Refreshing, state.status)
        assertEquals(1f, state.progress, 0.001f)
        refreshGate.complete(Unit)
    }

    @Test
    fun refreshingContentCanBeLockedExplicitly() {
        val refreshGate = CompletableDeferred<Unit>()
        val state = newState(
            contentScrollableWhileRefreshing = false,
            onRefresh = { refreshGate.await() },
        )
        state.consumeDownward(200f)
        state.refresh()
        val progressBeforeScroll = state.progress

        val consumed = state.consumePreScroll(-80f)

        assertEquals(-80f, consumed, 0.001f)
        assertEquals(progressBeforeScroll, state.progress, 0.001f)
        refreshGate.complete(Unit)
    }

    @Test
    fun refreshExceptionIsReported() {
        val expected = IllegalStateException("refresh failed")
        var reported: Throwable? = null
        val state = newState(
            onRefresh = { throw expected },
            onRefreshError = { reported = it },
        )

        state.refresh()

        assertSame(expected, reported)
    }

    private fun newState(
        triggerPx: Float = 100f,
        maxPullPx: Float = 200f,
        dragRate: Float = 0.5f,
        contentScrollableWhileRefreshing: Boolean = true,
        onRefresh: suspend PullRefreshCallbackScope.() -> Unit = {},
        onRefreshError: (Throwable) -> Unit = {},
    ): PullRefreshState = PullRefreshState(
        onRefresh = onRefresh,
        onRefreshError = onRefreshError,
        scope = scope,
        triggerDistancePx = triggerPx,
        maxPullDistancePx = maxPullPx,
        dragRate = dragRate,
        triggerDistance = with(Density(1f)) { triggerPx.toDp() },
        maxPullDistance = with(Density(1f)) { maxPullPx.toDp() },
        contentScrollableWhileRefreshing = contentScrollableWhileRefreshing,
        finishDelayMillis = 0,
        reboundSpec = spring(),
    )

    private class ImmediateFrameClock : MonotonicFrameClock {
        private var frameTimeNanos = 0L

        override val key: CoroutineContext.Key<*>
            get() = MonotonicFrameClock

        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
            frameTimeNanos += 16_000_000L
            return onFrame(frameTimeNanos)
        }
    }
}
