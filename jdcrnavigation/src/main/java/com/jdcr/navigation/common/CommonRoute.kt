package com.jdcr.navigation.common

import androidx.navigation3.runtime.NavKey
import com.jdcr.navigation.common.auth.LoginOptions
import com.jdcr.navigation.route.BaseAppRoute
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface CommonRoute : BaseAppRoute {

    @Serializable
    object Splash : CommonRoute

    @Serializable
    data class Login(
        @Polymorphic
        val returnTo: NavKey? = null,
        val options: LoginOptions = LoginOptions(),
    ) : CommonRoute

}

val commonRouteSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(
            CommonRoute.Splash::class,
            CommonRoute.Splash.serializer(),
        )
        subclass(
            CommonRoute.Login::class,
            CommonRoute.Login.serializer(),
        )
    }
}