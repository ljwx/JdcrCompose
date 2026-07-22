package com.jdcr.jdcrcompose.navigation.interceptor

import com.jdcr.jdcrcompose.navigation.route.BaseAppRoute

interface NavigationInterceptor {

    fun intercept(route: BaseAppRoute): BaseAppRoute

}