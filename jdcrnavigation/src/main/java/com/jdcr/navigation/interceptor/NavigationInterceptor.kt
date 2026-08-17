package com.jdcr.navigation.interceptor

import com.jdcr.navigation.route.BaseAppRoute

interface NavigationInterceptor {

    fun intercept(route: BaseAppRoute): BaseAppRoute

}
