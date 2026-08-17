# JdcrNavigation

基于 AndroidX Navigation 3 的 Compose 导航封装，提供类型安全路由、返回栈保存、统一导航操作、路由拦截、返回栈保护和可选的外部导航入口。

## 最小接入

```kotlin
AppNavHost(
    startRoute = AppRoute.Home,
    savedStateConfiguration = navSavedStateConfiguration,
) { navigator ->
    entry<AppRoute.Home> {
        HomeScreen(
            onOpenDetail = { id ->
                navigator.navigate(AppRoute.Detail(id))
            },
        )
    }

    entry<AppRoute.Detail> { route ->
        DetailScreen(
            id = route.id,
            onBack = navigator::back,
        )
    }
}
```

页面内优先使用 `AppNavigator`。只有通知、深链或 Activity 回调等 Composition 外部入口需要导航时，才创建并传入 `ExternalNavigationDispatcher`。

路由定义、多态序列化、登录拦截、返回栈保护、转场策略和完整示例见 [Navigation 3 与 JdcrNavigation 设计和使用指南](docs/NAVIGATION_GUIDE.md)。
