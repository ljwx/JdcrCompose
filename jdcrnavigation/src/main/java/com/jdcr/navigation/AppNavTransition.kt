package com.jdcr.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import com.jdcr.navigation.route.BaseAppRoute

/**
 * Selects an animation for a concrete route-to-route transition.
 *
 * [resolve] may be called more than once for the same transition and must be side-effect free.
 */
fun interface AppNavTransitionPolicy {
    fun resolve(
        fromRoute: BaseAppRoute,
        toRoute: BaseAppRoute,
    ): AppNavAnimation
}

/** Reusable animations understood by [AppNavHost]. */
@Immutable
sealed interface AppNavAnimation {

    /** Slides forward in [forwardDirection] and reverses that direction when popping. */
    data class Slide(
        val forwardDirection: AppNavSlideDirection = AppNavSlideDirection.Left,
        val durationMillis: Int = 300,
    ) : AppNavAnimation {
        init {
            require(durationMillis >= 0) { "durationMillis must be non-negative" }
        }
    }

    data class Fade(
        val enterDurationMillis: Int = 220,
        val exitDurationMillis: Int = 220,
    ) : AppNavAnimation {
        init {
            require(enterDurationMillis >= 0) {
                "enterDurationMillis must be non-negative"
            }
            require(exitDurationMillis >= 0) {
                "exitDurationMillis must be non-negative"
            }
        }
    }

    data object None : AppNavAnimation
}

enum class AppNavSlideDirection {
    Left,
    Right,
    Up,
    Down,
}

internal fun AnimatedContentTransitionScope<Scene<NavKey>>.contentTransform(
    animation: AppNavAnimation,
    isPop: Boolean,
): ContentTransform = when (animation) {
    is AppNavAnimation.Slide -> {
        val direction = animation.forwardDirection
            .let { if (isPop) it.opposite() else it }
            .toComposeDirection()
        val animationSpec = tween<IntOffset>(animation.durationMillis)
        slideIntoContainer(
            towards = direction,
            animationSpec = animationSpec,
        ) togetherWith slideOutOfContainer(
            towards = direction,
            animationSpec = animationSpec,
        )
    }

    is AppNavAnimation.Fade -> fadeIn(
        animationSpec = tween(animation.enterDurationMillis),
    ) togetherWith fadeOut(
        animationSpec = tween(animation.exitDurationMillis),
    )

    AppNavAnimation.None -> EnterTransition.None togetherWith ExitTransition.None
}

private fun AppNavSlideDirection.opposite(): AppNavSlideDirection = when (this) {
    AppNavSlideDirection.Left -> AppNavSlideDirection.Right
    AppNavSlideDirection.Right -> AppNavSlideDirection.Left
    AppNavSlideDirection.Up -> AppNavSlideDirection.Down
    AppNavSlideDirection.Down -> AppNavSlideDirection.Up
}

private fun AppNavSlideDirection.toComposeDirection():
    AnimatedContentTransitionScope.SlideDirection = when (this) {
    AppNavSlideDirection.Left -> AnimatedContentTransitionScope.SlideDirection.Left
    AppNavSlideDirection.Right -> AnimatedContentTransitionScope.SlideDirection.Right
    AppNavSlideDirection.Up -> AnimatedContentTransitionScope.SlideDirection.Up
    AppNavSlideDirection.Down -> AnimatedContentTransitionScope.SlideDirection.Down
}
