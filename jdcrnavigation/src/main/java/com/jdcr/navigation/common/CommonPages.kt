package com.jdcr.navigation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.jdcr.navigation.AppNavigator
import com.jdcr.navigation.common.auth.AuthLoginState
import com.jdcr.navigation.common.auth.AuthLoginType
import com.jdcr.navigation.common.auth.AuthService
import com.jdcr.navigation.common.auth.AuthSessionState
import com.jdcr.navigation.common.auth.LoginMainCoordinator
import com.jdcr.navigation.common.auth.LoginOptions
import com.jdcr.navigation.common.splash.SplashCoordinator
import com.jdcr.navigation.common.splash.SplashUiInitializer
import com.jdcr.navigation.common.splash.SplashUiState
import com.jdcr.navigation.route.BaseAppRoute


@Immutable
data class CommonPages(
    val splash: @Composable (state: SplashUiState, onRetry: () -> Unit) -> Unit,
    val login: @Composable (
        options: LoginOptions,
        state: AuthLoginState,
        onLogin: (AuthLoginType) -> Unit,
        onCancel: () -> Unit,
    ) -> Unit,
)

fun EntryProviderScope<NavKey>.installCommonPages(
    navigator: AppNavigator,
    pages: CommonPages,
    initializer: SplashUiInitializer,
    authService: AuthService,
    defaultAfterLogin: BaseAppRoute,
    onLoginCancel: (CommonRoute.Login) -> Unit = {
        navigator.back()
    },
) {
    entry<CommonRoute.Splash> {
        SplashEntry(
            navigator = navigator,
            pages = pages,
            initializer = initializer,
        )
    }

    entry<CommonRoute.Login> { route ->
        LoginEntry(
            route = route,
            navigator = navigator,
            pages = pages,
            authService = authService,
            defaultAfterLogin = defaultAfterLogin,
            onCancel = { onLoginCancel(route) },
        )
    }

}

@Composable
private fun SplashEntry(
    navigator: AppNavigator,
    pages: CommonPages,
    initializer: SplashUiInitializer,
) {
    val coordinator = viewModel {
        SplashCoordinator(initializer)
    }
    val state by coordinator.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        val completed = state as? SplashUiState.Completed ?: return@LaunchedEffect

        navigator.resetTo(completed.destination)
    }

    pages.splash(state, coordinator::retry)
}

@Composable
private fun LoginEntry(
    route: CommonRoute.Login,
    navigator: AppNavigator,
    pages: CommonPages,
    authService: AuthService,
    defaultAfterLogin: BaseAppRoute,
    onCancel: () -> Unit,
) {
    val coordinator = viewModel { LoginMainCoordinator(authService) }
    val state by coordinator.state.collectAsStateWithLifecycle()
    val sessionState by authService.sessionState.collectAsStateWithLifecycle()

    val shouldFinishLogin =
        state is AuthLoginState.Success ||
            (state is AuthLoginState.Idle &&
                sessionState is AuthSessionState.Authenticated)

    LaunchedEffect(shouldFinishLogin, route) {
        if (!shouldFinishLogin) return@LaunchedEffect

        val returnTo = route.returnTo
        if (returnTo == null) {
            navigator.resetTo(defaultAfterLogin)
        } else {
            require(returnTo is BaseAppRoute) {
                "Login returnTo must implement BaseAppRoute: $returnTo"
            }
            navigator.replace(returnTo)
        }
    }

    pages.login(route.options, state, coordinator::login, onCancel)
}
