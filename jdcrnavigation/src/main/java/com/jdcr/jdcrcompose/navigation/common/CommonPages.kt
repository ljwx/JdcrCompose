package com.jdcr.jdcrcompose.navigation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.jdcr.jdcrcompose.navigation.AppNavigator
import com.jdcr.jdcrcompose.navigation.common.login.AuthLoginState
import com.jdcr.jdcrcompose.navigation.common.login.AuthLoginType
import com.jdcr.jdcrcompose.navigation.common.login.AuthService
import com.jdcr.jdcrcompose.navigation.common.login.LoginCoordinator
import com.jdcr.jdcrcompose.navigation.common.splash.AppStateInitializer
import com.jdcr.jdcrcompose.navigation.common.splash.SplashCoordinator
import com.jdcr.jdcrcompose.navigation.common.splash.SplashUiState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@Immutable
data class CommonPages(
    val splash: @Composable (state: SplashUiState) -> Unit,
    val login: @Composable (
        state: AuthLoginState,
        onAccountChanged: (AuthLoginType) -> Unit,
        onLogin: () -> Unit,
    ) -> Unit,
    val main: @Composable () -> Unit,
)

fun EntryProviderScope<NavKey>.installCommonPage(
    navigator: AppNavigator,
    screens: CommonPages,
    initializer: AppStateInitializer,
    authService: AuthService,
) {
    entry<CommonRoute.Splash> {
        screens.splash(SplashUiState.Initializing)
    }

//    entry<CommonRoute.Splash> {
//        val coroutineScope = rememberCoroutineScope()
//        val coordinator = remember(
//            initializer,
//            CommonRoute.HomeMain,
//        ) {
//            SplashCoordinator(
//                initializer = initializer,
//                destination = CommonRoute.HomeMain,
//            )
//        }
//        val state by coordinator.state.collectAsState()
//        LaunchedEffect(coordinator) {
//            coordinator.initialize()
//        }
//        LaunchedEffect(state) {
//            val completed =
//                state as? SplashUiState.Completed
//                    ?: return@LaunchedEffect
//            navigator.resetTo(completed.destination)
//        }
//        screens.splash(
//            state = state,
//            onRetry = {
//                coroutineScope.launch {
//                    coordinator.initialize()
//                }
//            },
//        )
//    }
//    entry<CommonRoute.LoginMain> {
//        val coroutineScope = rememberCoroutineScope()
//        val coordinator = remember(authService) {
//            LoginCoordinator(authService)
//        }
//        val state by coordinator.state.collectAsState()
//        LaunchedEffect(state.loginSuccessful) {
//            if (state.loginSuccessful) {
//                navigator.resetTo(CommonRoute.HomeMain)
//            }
//        }
//        screens.login(
//            state = state,
//            onAccountChanged = coordinator::updateAccount,
//            onPasswordChanged = coordinator::updatePassword,
//            onLogin = {
//                coroutineScope.launch {
//                    coordinator.login()
//                }
//            },
//        )
//    }
//    entry<CommonRoute.HomeMain> {
//        screens.main()
//    }
}