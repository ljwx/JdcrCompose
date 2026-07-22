package com.jdcr.jdcrcompose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.jdcr.jdcrcompose.navigation.command.ExternalNavigationCommand
import com.jdcr.jdcrcompose.navigation.command.ExternalNavigationDispatcher
import com.jdcr.jdcrcompose.navigation.interceptor.NavigationInterceptor
import com.jdcr.jdcrcompose.navigation.route.BaseAppRoute

typealias DestinationRegistry =
        EntryProviderScope<NavKey>.(AppNavigator) -> Unit

@Composable
fun AppNavHost(
    startRoute: BaseAppRoute,
    savedStateConfiguration: SavedStateConfiguration,
    externalDispatcher: ExternalNavigationDispatcher,
    interceptors: List<NavigationInterceptor> = emptyList(),
    destinations: DestinationRegistry,
) {

    val backStack = rememberNavBackStack(savedStateConfiguration, startRoute)
    val navigator = remember(backStack, interceptors) {
        DefaultAppNavigator(
            backStack = backStack,
            interceptors = interceptors,
        )
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
        NavDisplay(
            backStack = backStack,
            onBack = { navigator.back() },
            entryDecorators = listOf(
                // 商用必加：页面 rememberSaveable 状态恢复
                rememberSaveableStateHolderNavEntryDecorator(),
                // 商用必加：ViewModel 作用域按 NavEntry 管理
//            rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                destinations(navigator)
            }
        )
    }

}