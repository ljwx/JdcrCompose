package com.jdcr.jdcrpullrefresh

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/** Text labels for the built-in classic Header. */
@Immutable
data class PullRefreshHeaderLabels(
    val pulling: String = "Pull to refresh",
    val ready: String = "Release to refresh",
    val refreshing: String = "Refreshing...",
    val complete: String = "Refresh complete",
    val failed: String = "Refresh failed",
)

/**
 * A small, neutral Header inspired by SmartRefreshLayout's ClassicsHeader.
 *
 * It is intentionally just a convenience default. A product-specific Header can use the same
 * [PullRefreshHeaderScope] and replace this slot without changing the refresh mechanics.
 */
@Composable
fun PullRefreshHeaderScope.JdcrClassicHeader(
    modifier: Modifier = Modifier,
    labels: PullRefreshHeaderLabels = PullRefreshHeaderLabels(),
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (status == PullRefreshStatus.ReadyToRefresh) 180f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "pull-refresh-arrow",
    )

    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (status) {
            PullRefreshStatus.Refreshing -> CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = tint,
                strokeWidth = 2.dp,
            )

            else -> PullRefreshArrow(
                modifier = Modifier.size(22.dp),
                rotation = arrowRotation,
                progress = progress,
                color = tint,
            )
        }

        Spacer(Modifier.size(10.dp))
        Crossfade(
            targetState = status,
            animationSpec = tween(durationMillis = 120),
            label = "pull-refresh-label",
        ) { currentStatus ->
            Text(
                text = when (currentStatus) {
                    PullRefreshStatus.Idle,
                    PullRefreshStatus.Pulling,
                    -> labels.pulling

                    PullRefreshStatus.ReadyToRefresh -> labels.ready
                    PullRefreshStatus.Refreshing -> labels.refreshing
                    PullRefreshStatus.RefreshComplete -> labels.complete
                    PullRefreshStatus.RefreshFailed -> labels.failed
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PullRefreshArrow(
    modifier: Modifier,
    rotation: Float,
    progress: Float,
    color: Color,
) {
    Canvas(modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val length = (size.minDimension * (0.24f + progress.coerceIn(0f, 1f) * 0.2f))
            .coerceAtMost(size.minDimension * 0.42f)
        val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)

        rotate(rotation, pivot = Offset(centerX, centerY)) {
            drawLine(
                color = color,
                start = Offset(centerX, centerY - length),
                end = Offset(centerX, centerY + length),
                strokeWidth = stroke.width,
                cap = stroke.cap,
            )
            drawLine(
                color = color,
                start = Offset(centerX, centerY + length),
                end = Offset(centerX - length * 0.65f, centerY + length * 0.35f),
                strokeWidth = stroke.width,
                cap = stroke.cap,
            )
            drawLine(
                color = color,
                start = Offset(centerX, centerY + length),
                end = Offset(centerX + length * 0.65f, centerY + length * 0.35f),
                strokeWidth = stroke.width,
                cap = stroke.cap,
            )
        }
    }
}
