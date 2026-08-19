# JdcrLoadMore 接入说明

`jdcrloadmore` 负责把 Paging 3 的追加状态转换成可定制 Footer，不维护页码，也不直接调用业务接口。

## 创建 Pager

```kotlin
val products = Pager(
    config = PagingConfig(
        pageSize = 20,
        prefetchDistance = 3,
        enablePlaceholders = false,
    ),
    pagingSourceFactory = { ProductPagingSource(repository) },
).flow.cachedIn(viewModelScope)
```

## 在列表中使用

```kotlin
val products = viewModel.products.collectAsLazyPagingItems()

LazyColumn {
    items(
        count = products.itemCount,
        key = products.itemKey(Product::id),
    ) { index ->
        products[index]?.let { product ->
            ProductItem(product)
        }
    }

    pagingLoadMoreFooter(products)
}
```

访问 `products[index]` 时，Paging 3 会根据 `prefetchDistance` 自动判断是否追加下一页。不要在
Footer 中再次调用页码接口，否则会形成两套分页状态。

## 在网格或自定义容器中使用

`pagingLoadMoreFooter()` 是 `LazyColumn` 的便捷扩展。其他容器可以使用同一套状态转换，
不需要在业务层重复判断 `LoadState`：

```kotlin
LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Adaptive(156.dp)) {
    items(
        count = products.itemCount,
        key = products.itemKey(Product::id),
    ) { index ->
        products[index]?.let { ProductItem(it) }
    }

    item(span = StaggeredGridItemSpan.FullLine) {
        JdcrLoadMoreFooter(
            state = products.loadMoreUiState(),
            onRetry = products::retry,
        )
    }
}
```

`loadMoreUiState()` 只转换 Paging 状态，因此也可以用于普通网格或业务自己的 Footer 布局。

## 自定义 Footer

```kotlin
pagingLoadMoreFooter(products) {
    when (state) {
        LoadMoreUiState.Loading -> LoadingFooter()
        is LoadMoreUiState.Error -> ErrorFooter(onRetry = ::retry)
        LoadMoreUiState.End -> EndFooter()
        LoadMoreUiState.Idle -> Unit
    }
}
```

也可以继续使用默认样式，只替换文字：

```kotlin
pagingLoadMoreFooter(products) {
    JdcrClassicLoadMoreFooter(
        labels = LoadMoreFooterLabels(
            loading = "正在加载更多",
            failed = "加载失败",
            retry = "重试",
            end = "已经到底了",
        ),
    )
}
```

## 状态边界

- `loadState.refresh` 用于页面首屏加载、首屏失败和下拉刷新。
- `loadState.append` 用于加载更多 Footer。
- `retry()` 只重试失败的 Paging 请求。
- `refresh()` 会创建新的 `PagingData`，不应当作为追加下一页的方法。
- 空列表不会展示“没有更多”，应由页面自己的空状态处理。
