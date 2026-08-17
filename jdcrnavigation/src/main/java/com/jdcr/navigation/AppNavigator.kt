package com.jdcr.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.jdcr.navigation.interceptor.NavigationInterceptor
import com.jdcr.navigation.route.BaseAppRoute

@Stable
interface AppNavigator {
    fun navigate(route: BaseAppRoute, singleTop: Boolean = true)
    fun back()
    fun replace(route: BaseAppRoute)
    fun resetTo(route: BaseAppRoute)
    fun popTo(route: BaseAppRoute, inclusive: Boolean = false)
}

class DefaultAppNavigator(
    private val backStack: NavBackStack<NavKey>,
    private val interceptors: List<NavigationInterceptor>,
) : AppNavigator {

    private fun resolve(route: BaseAppRoute): BaseAppRoute {
        return interceptors.fold(route) { current, interceptor ->
            interceptor.intercept(current)
        }
    }

    override fun navigate(route: BaseAppRoute, singleTop: Boolean) {
        val target = resolve(route)
        if (singleTop && backStack.lastOrNull() == target) {
            return
        }
        backStack.add(target)
    }

    override fun back() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    override fun replace(route: BaseAppRoute) {
        val target = resolve(route)
        if (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.lastIndex)
        }
        backStack.add(target)
    }

    override fun resetTo(route: BaseAppRoute) {
        val target = resolve(route)
        backStack.clear()
        backStack.add(target)
    }

    override fun popTo(route: BaseAppRoute, inclusive: Boolean) {
        val index = backStack.indexOfLast { it == route }
        if (index < 0) return
        // NavDisplay 不接受空栈，根页面始终保留。
        val targetSize = (if (inclusive) index else index + 1).coerceAtLeast(1)
        while (backStack.size > targetSize) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
}

val LocalAppNavigator = staticCompositionLocalOf<AppNavigator> {
    error("没有导航路由,请确认路由是否配置")
}
