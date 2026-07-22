package com.jdcr.jdcrcompose.navigation.interceptor

import com.jdcr.jdcrcompose.navigation.route.BaseAppRoute
import com.jdcr.jdcrcompose.navigation.common.CommonRoute
import com.jdcr.jdcrcompose.navigation.route.LoginRequired

class AuthNavigationInterceptor<T>(
    private val userSession: UserSession,
) : NavigationInterceptor {
    override fun intercept(route: BaseAppRoute): BaseAppRoute {
        return if (route is LoginRequired && !userSession.isLoggedIn) {
            CommonRoute.LoginMain(userSession.loginParams())
        } else {
            route
        }
    }
}