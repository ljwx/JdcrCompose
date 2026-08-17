package com.jdcr.jdcrcompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jdcr.jdcrcompose.data.ProductCatalog
import com.jdcr.jdcrcompose.navigation.DemoRoute
import com.jdcr.jdcrcompose.navigation.demoSavedStateConfiguration
import com.jdcr.jdcrcompose.navigation.evaluateDemoBackStack
import com.jdcr.jdcrcompose.ui.screen.LoginScreen
import com.jdcr.jdcrcompose.ui.screen.ProductDetailScreen
import com.jdcr.jdcrcompose.ui.screen.ProductListScreen
import com.jdcr.jdcrcompose.ui.screen.SplashScreen
import com.jdcr.navigation.AppNavHost
import com.jdcr.navigation.AppNavAnimation
import com.jdcr.navigation.AppNavTransitionPolicy
import com.jdcr.navigation.common.CommonPages
import com.jdcr.navigation.common.CommonRoute
import com.jdcr.navigation.common.auth.AuthSessionState
import com.jdcr.navigation.common.auth.LoginMethod
import com.jdcr.navigation.common.auth.LoginOptions
import com.jdcr.navigation.common.auth.LoginReason
import com.jdcr.navigation.common.installCommonPages
import com.jdcr.navigation.interceptor.AuthNavigationInterceptor

@Composable
fun DemoApp(
    appViewModel: DemoAppViewModel = viewModel(),
) {
    val authService = appViewModel.authService
    val sessionState by authService.sessionState.collectAsStateWithLifecycle()
    val unauthenticatedReason by authService.unauthenticatedReason.collectAsStateWithLifecycle()
    val interceptors = remember(authService) {
        listOf(
            AuthNavigationInterceptor(authService) {
                LoginOptions(initialMethod = LoginMethod.Account)
            },
        )
    }
    val commonPages = remember {
        CommonPages(
            splash = { state, onRetry ->
                SplashScreen(
                    state = state,
                    onRetry = onRetry,
                )
            },
            login = { options, state, onLogin, onCancel ->
                LoginScreen(
                    options = options,
                    state = state,
                    onLogin = onLogin,
                    onLeave = onCancel,
                )
            },
        )
    }
    val transitionPolicy = remember {
        AppNavTransitionPolicy { fromRoute, toRoute ->
            when {
                fromRoute is CommonRoute.Splash || toRoute is CommonRoute.Splash ->
                    AppNavAnimation.None

                fromRoute is CommonRoute.Login || toRoute is CommonRoute.Login ->
                    AppNavAnimation.Fade()

                else -> AppNavAnimation.Slide()
            }
        }
    }

    AppNavHost(
        startRoute = CommonRoute.Splash,
        savedStateConfiguration = demoSavedStateConfiguration,
        externalDispatcher = appViewModel.externalNavigationDispatcher,
        interceptors = interceptors,
        backStackGuard = { backStack ->
            evaluateDemoBackStack(
                backStack = backStack,
                sessionState = sessionState,
                unauthenticatedReason = unauthenticatedReason,
            )
        },
        guardPlaceholder = {
            SplashScreen(
                state = com.jdcr.navigation.common.splash.SplashUiState.Initializing,
                onRetry = {},
            )
        },
        transitionPolicy = transitionPolicy,
    ) { navigator ->
        installCommonPages(
            navigator = navigator,
            pages = commonPages,
            initializer = appViewModel.splashInitializer,
            authService = authService,
            defaultAfterLogin = DemoRoute.Home,
            onLoginCancel = {
                navigator.resetTo(DemoRoute.Home)
            },
        )

        entry<DemoRoute.Home> {
            ProductListScreen(
                products = ProductCatalog.products,
                isLoggedIn = sessionState is AuthSessionState.Authenticated,
                onProductClick = { productId ->
                    navigator.navigate(DemoRoute.ProductDetail(productId))
                },
                onActiveLogin = {
                    navigator.navigate(
                        CommonRoute.Login(
                            options = LoginOptions(
                                initialMethod = LoginMethod.Account,
                                reason = LoginReason.UserInitiated,
                            ),
                        ),
                    )
                },
                onLogout = appViewModel::logout,
                onRefresh = appViewModel::refreshProducts,
            )
        }

        entry<DemoRoute.ProductDetail> { route ->
            ProductDetailScreen(
                product = ProductCatalog.find(route.productId),
                onBack = navigator::back,
                onExpireSession = appViewModel::expireSession,
            )
        }
    }
}
