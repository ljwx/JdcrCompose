package com.jdcr.jdcrcompose.navigation.command

import com.jdcr.jdcrcompose.navigation.route.BaseAppRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface ExternalNavigationCommand {

    data class Navigate(val route: BaseAppRoute, val singleTop: Boolean = true) :
        ExternalNavigationCommand

    data class Replace(val route: BaseAppRoute) : ExternalNavigationCommand

    data class Reset(val route: BaseAppRoute) : ExternalNavigationCommand

    object Back : ExternalNavigationCommand

    data class Pop(val route: BaseAppRoute, val inclusive: Boolean = false) :
        ExternalNavigationCommand

}

class ExternalNavigationDispatcher {
    private val channel = Channel<ExternalNavigationCommand>(Channel.UNLIMITED)
    internal val commands = channel.receiveAsFlow()

    fun navigate(route: BaseAppRoute, singleTop: Boolean = true) {
        channel.trySend(ExternalNavigationCommand.Navigate(route, singleTop))
    }

    fun replace(route: BaseAppRoute) {
        channel.trySend(ExternalNavigationCommand.Replace(route))
    }

    fun reset(route: BaseAppRoute) {
        channel.trySend(ExternalNavigationCommand.Reset(route))
    }

    fun back() {
        channel.trySend(ExternalNavigationCommand.Back)
    }

    fun pop(route: BaseAppRoute, inclusive: Boolean = false) {
        channel.trySend(ExternalNavigationCommand.Pop(route, inclusive))
    }

}