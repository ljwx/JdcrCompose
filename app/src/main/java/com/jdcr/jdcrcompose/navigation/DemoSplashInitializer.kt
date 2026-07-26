package com.jdcr.jdcrcompose.navigation

import com.jdcr.navigation.common.splash.SplashUiInitializer
import com.jdcr.navigation.route.BaseAppRoute
import kotlinx.coroutines.delay

class DemoSplashInitializer : SplashUiInitializer {
    override suspend fun initialize(): Result<BaseAppRoute> {
        delay(SPLASH_DURATION_MILLIS)
        return Result.success(DemoRoute.Home)
    }

    private companion object {
        const val SPLASH_DURATION_MILLIS = 2_000L
    }
}
