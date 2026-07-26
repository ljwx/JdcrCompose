package com.jdcr.navigation.interceptor

import com.jdcr.navigation.route.BaseAppRoute
import com.jdcr.navigation.common.CommonRoute
import com.jdcr.navigation.common.auth.AuthService
import com.jdcr.navigation.common.auth.AuthSessionState
import com.jdcr.navigation.common.auth.LoginOptions
import com.jdcr.navigation.common.auth.LoginReason
import com.jdcr.navigation.route.RequiresLogin

class AuthNavigationInterceptor(
    private val authService: AuthService,
    private val loginOptions: (BaseAppRoute) -> LoginOptions = {
        LoginOptions(reason = LoginReason.ProtectedRoute)
    },
) : NavigationInterceptor {
    override fun intercept(route: BaseAppRoute): BaseAppRoute {
        if (route !is RequiresLogin) return route

        return when (authService.sessionState.value) {
            AuthSessionState.Authenticated -> route

            AuthSessionState.Unauthenticated -> CommonRoute.Login(
                returnTo = route,
                options = loginOptions(route).copy(
                    reason = LoginReason.ProtectedRoute,
                ),
            )

            // guard 会隐藏页面，认证完成后再决定允许还是重置。
            AuthSessionState.Checking -> route
        }
    }
}
