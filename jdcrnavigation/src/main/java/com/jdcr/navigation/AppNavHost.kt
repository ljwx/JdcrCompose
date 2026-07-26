package com.jdcr.navigation

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
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.jdcr.navigation.command.ExternalNavigationCommand
import com.jdcr.navigation.command.ExternalNavigationDispatcher
import com.jdcr.navigation.interceptor.NavigationInterceptor
import com.jdcr.navigation.route.BaseAppRoute

typealias DestinationRegistry =
        EntryProviderScope<NavKey>.(AppNavigator) -> Unit

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
