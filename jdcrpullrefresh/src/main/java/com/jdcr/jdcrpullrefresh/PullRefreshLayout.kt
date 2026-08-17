package com.jdcr.jdcrpullrefresh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Velocity

/** Values a Header can use to render its own pull and refresh animation. */
@Stable
class PullRefreshHeaderScope internal constructor(
    private val state: PullRefreshState,
    private val density: Density,
) {
    val status: PullRefreshStatus
        get() = state.status

    val progress: Float
        get() = state.progress

    val isRefreshing: Boolean
        get() = state.isRefreshing

    val pullDistance: Dp
        get() = state.pullDistance(density)

    val triggerDistance: Dp
        get() = state.triggerDistance

    val maxPullDistance: Dp
        get() = state.maxPullDistance
}

/**
 * A SmartRefresh-style pull-to-refresh container for Compose scrollables.
 *
 * Put a `LazyColumn`, `verticalScroll` content, or another vertical nested-scroll child in
 * [content]. The child scrolls normally; once it is at the top, this container applies a damped
 * pull, moves the content and reveals [header] together, and starts the callback after release
 * past the threshold.
 */
@Composable
fun JdcrPullRefresh(
    state: PullRefreshState,
    modifier: Modifier = Modifier,
    headerHeight: Dp = state.triggerDistance,
    header: @Composable PullRefreshHeaderScope.() -> Unit = {
        JdcrClassicHeader()
    },
    content: @Composable BoxScope.() -> Unit,
) {
    require(headerHeight > 0.dp) { "headerHeight must be greater than zero" }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val headerScope = remember(state, density) {
        PullRefreshHeaderScope(state, density)
    }
    val nestedScrollConnection = remember(state) {
        state.rememberNestedScrollConnection()
    }
    val headerHeightPx = with(density) { headerHeight.toPx() }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { state.updateContainerHeight(it.height.toFloat()) }
            .nestedScroll(nestedScrollConnection),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .graphicsLayer {
                    translationY = state.indicatorOffsetPx - headerHeightPx
                },
            contentAlignment = Alignment.Center,
        ) {
            headerScope.header()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = state.contentOffsetPx
                },
            content = content,
        )
    }
}

private fun PullRefreshState.rememberNestedScrollConnection(): NestedScrollConnection =
    object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (source != NestedScrollSource.UserInput) return Offset.Zero
            val consumed = consumePreScroll(available.y)
            return if (consumed == 0f) Offset.Zero else Offset(0f, consumed)
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (source != NestedScrollSource.UserInput) return Offset.Zero
            val consumedByRefresh = consumeDownward(available.y)
            return if (consumedByRefresh == 0f) Offset.Zero else Offset(0f, consumedByRefresh)
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            return if (release(available.y)) available else Velocity.Zero
        }
    }
