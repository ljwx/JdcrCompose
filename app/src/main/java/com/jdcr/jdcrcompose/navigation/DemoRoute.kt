package com.jdcr.jdcrcompose.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.jdcr.navigation.common.commonRouteSerializersModule
import com.jdcr.navigation.route.BaseAppRoute
import com.jdcr.navigation.route.RequiresLogin
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed interface DemoRoute : BaseAppRoute {
    @Serializable
    data object Home : DemoRoute

    @Serializable
    data class ProductDetail(
        val productId: Long,
    ) : DemoRoute, RequiresLogin
}

val demoSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        include(commonRouteSerializersModule)
        polymorphic(NavKey::class) {
            subclass(DemoRoute.Home::class, DemoRoute.Home.serializer())
            subclass(
                DemoRoute.ProductDetail::class,
                DemoRoute.ProductDetail.serializer(),
            )
        }
    }
}
