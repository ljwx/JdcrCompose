package com.jdcr.jdcrcompose.navigation.common

import androidx.navigation3.runtime.NavKey
import com.jdcr.jdcrcompose.navigation.route.BaseAppRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface CommonRoute : BaseAppRoute {

    @Serializable
    object Splash : CommonRoute

    @Serializable
    data class HomeMain(val data: String? = null) : CommonRoute

    @Serializable
    data class LoginMain(val data: String? = null) : CommonRoute

}

val commonRouteSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(
            CommonRoute.Splash::class,
            CommonRoute.Splash.serializer(),
        )
        subclass(
            CommonRoute.HomeMain::class,
            CommonRoute.HomeMain.serializer(),
        )
        subclass(
            CommonRoute.LoginMain::class,
            CommonRoute.LoginMain.serializer(),
        )
    }
}