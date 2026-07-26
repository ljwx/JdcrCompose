package com.jdcr.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.jdcr.navigation.command.ExternalNavigationCommand
import com.jdcr.navigation.command.ExternalNavigationDispatcher
import com.jdcr.navigation.interceptor.NavigationInterceptor
import com.jdcr.navigation.route.BaseAppRoute

typealias DestinationRegistry =
        EntryProviderScope<NavKey>.(AppNavigator) -> Unit

typealias AppNavTransitionSpec =
        AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform

typealias AppNavPredictivePopTransitionSpec =
        AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform

@Composable
fun AppNavHost(
    startRoute: BaseAppRoute,
    savedStateConfiguration: SavedStateConfiguration,
    externalDispatcher: ExternalNavigationDispatcher,
    interceptors: List<NavigationInterceptor> = emptyList(),
    backStackGuard: BackStackGuard = {
        BackStackGuardResult.Allow
    },
    guardPlaceholder: @Composable () -> Unit = {},
    // 普通前进：新页面从右侧进入
    transitionSpec: AppNavTransitionSpec = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(300),
        ) togetherWith slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(300),
        )
    },

    // 普通返回：当前页面向右退出
    popTransitionSpec: AppNavTransitionSpec = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(300),
        ) togetherWith slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(300),
        )
    },

    // 预测返回复用普通返回的完整位移曲线：取消时回到原位，
    // 确认时从当前手势进度继续向右退出，避免切换动画时发生跳变。
    predictivePopTransitionSpec: AppNavPredictivePopTransitionSpec = { _ ->
        popTransitionSpec(this)
    },
    destinations: DestinationRegistry,
) {

    val backStack = rememberNavBackStack(savedStateConfiguration, startRoute)
    val navigator = remember(backStack, interceptors) {
        DefaultAppNavigator(
            backStack = backStack,
            interceptors = interceptors,
        )
    }
    val guardResult = backStackGuard(backStack)
    LaunchedEffect(guardResult, navigator) {
        when (val result = guardResult) {
            is BackStackGuardResult.Reset -> navigator.resetTo(result.route)
            is BackStackGuardResult.ReplaceTop -> navigator.replace(result.route)
            BackStackGuardResult.Allow, BackStackGuardResult.Pending -> Unit
        }
    }

    LaunchedEffect(externalDispatcher, navigator) {
        externalDispatcher.commands.collect { command ->
            when (command) {
                is ExternalNavigationCommand.Navigate ->
                    navigator.navigate(command.route, command.singleTop)

                is ExternalNavigationCommand.Replace ->
                    navigator.replace(command.route)

                is ExternalNavigationCommand.Reset ->
                    navigator.resetTo(command.route)

                ExternalNavigationCommand.Back ->
                    navigator.back()

                is ExternalNavigationCommand.Pop -> navigator.popTo(
                    command.route,
                    command.inclusive
                )
            }
        }
    }

    CompositionLocalProvider(
        LocalAppNavigator provides navigator,
    ) {
        val entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator<NavKey>(),
        )

        val provider = remember(destinations, navigator) {
            entryProvider<NavKey> {
                destinations(navigator)
            }
        }

        val entries = rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = entryDecorators,
            entryProvider = provider,
        )
        when (guardResult) {
            BackStackGuardResult.Allow -> {
                NavDisplay(
                    entries = entries,
                    onBack = navigator::back,
                    transitionSpec = transitionSpec,
                    popTransitionSpec = popTransitionSpec,
                    predictivePopTransitionSpec = predictivePopTransitionSpec,
                )
            }

            BackStackGuardResult.Pending,
            is BackStackGuardResult.Reset,
            is BackStackGuardResult.ReplaceTop -> {
                guardPlaceholder()
            }
        }

    }

}

sealed interface BackStackGuardResult {
    data object Allow : BackStackGuardResult
    data object Pending : BackStackGuardResult
    data class Reset(val route: BaseAppRoute) : BackStackGuardResult
    data class ReplaceTop(val route: BaseAppRoute) : BackStackGuardResult
}

typealias BackStackGuard = (backStack: List<NavKey>) -> BackStackGuardResult
