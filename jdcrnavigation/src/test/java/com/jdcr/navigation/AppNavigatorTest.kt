package com.jdcr.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.jdcr.navigation.interceptor.NavigationInterceptor
import com.jdcr.navigation.route.BaseAppRoute
import org.junit.Assert.assertEquals
import org.junit.Test

class AppNavigatorTest {

    @Test
    fun `singleTop 不重复添加相同页面`() {
        val backStack = NavBackStack<NavKey>(TestRoute.Root)
        val navigator = DefaultAppNavigator(backStack, emptyList())

        navigator.navigate(TestRoute.Detail)
        navigator.navigate(TestRoute.Detail)

        assertEquals(listOf(TestRoute.Root, TestRoute.Detail), backStack)
    }

    @Test
    fun `包含根页面的 popTo 仍保留根页面`() {
        val backStack = NavBackStack<NavKey>(
            TestRoute.Root,
            TestRoute.Detail,
        )
        val navigator = DefaultAppNavigator(backStack, emptyList())

        navigator.popTo(TestRoute.Root, inclusive = true)

        assertEquals(listOf(TestRoute.Root), backStack)
    }

    @Test
    fun `包含普通页面的 popTo 会移除目标及其后页面`() {
        val backStack = NavBackStack<NavKey>(
            TestRoute.Root,
            TestRoute.Detail,
            TestRoute.Settings,
        )
        val navigator = DefaultAppNavigator(backStack, emptyList())

        navigator.popTo(TestRoute.Detail, inclusive = true)

        assertEquals(listOf(TestRoute.Root), backStack)
    }

    @Test
    fun `导航策略按声明顺序转换目标`() {
        val backStack = NavBackStack<NavKey>(TestRoute.Root)
        val interceptors = listOf(
            intercept(TestRoute.Detail, TestRoute.Settings),
            intercept(TestRoute.Settings, TestRoute.Login),
        )
        val navigator = DefaultAppNavigator(backStack, interceptors)

        navigator.navigate(TestRoute.Detail)

        assertEquals(listOf(TestRoute.Root, TestRoute.Login), backStack)
    }

    @Test
    fun `replace 和 reset 保持预期返回栈`() {
        val backStack = NavBackStack<NavKey>(TestRoute.Root, TestRoute.Detail)
        val navigator = DefaultAppNavigator(backStack, emptyList())

        navigator.replace(TestRoute.Settings)
        assertEquals(listOf(TestRoute.Root, TestRoute.Settings), backStack)

        navigator.resetTo(TestRoute.Login)
        assertEquals(listOf(TestRoute.Login), backStack)
    }

    private fun intercept(
        source: BaseAppRoute,
        target: BaseAppRoute,
    ) = object : NavigationInterceptor {
        override fun intercept(route: BaseAppRoute): BaseAppRoute =
            if (route == source) target else route
    }

    private sealed interface TestRoute : BaseAppRoute {
        data object Root : TestRoute
        data object Detail : TestRoute
        data object Settings : TestRoute
        data object Login : TestRoute
    }
}
