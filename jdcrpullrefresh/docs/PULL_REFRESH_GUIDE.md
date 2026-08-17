# JdcrPullRefresh

`JdcrPullRefresh` is a nested-scroll pull-to-refresh container. It lets the child consume normal
vertical scrolling first, then takes over the unconsumed downward drag at the top of the content.

## Basic usage

```kotlin
val refreshState = rememberPullRefreshState(
    onRefresh = {
        viewModel.refreshProducts() // suspend until the real refresh is complete
    },
    onRefreshError = logger::record,
)

JdcrPullRefresh(
    state = refreshState,
    modifier = Modifier.fillMaxSize(),
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        overscrollEffect = null,
    ) {
        // items
    }
}
```

An uncaught exception from `onRefresh` produces `RefreshFailed`. When the business layer handles
the error itself, finish explicitly from the callback:

```kotlin
val refreshState = rememberPullRefreshState(
    onRefresh = {
        runCatching { viewModel.refreshProducts() }
            .onFailure { finish(PullRefreshResult.Failure) }
    },
)
```

## Custom Header

The Header is a regular Compose slot. It receives state without depending on the content's
`LazyListState` or `ScrollState`.

```kotlin
JdcrPullRefresh(
    state = refreshState,
    headerHeight = 72.dp,
    header = {
        ProductRefreshHeader(
            status = status,
            progress = progress,
            pullDistance = pullDistance,
        )
    },
) {
    LazyColumn(Modifier.fillMaxSize()) {
        // items
    }
}
```

`progress == 1f` is the release threshold. It can exceed `1f` until `maxPullDistance`, which lets a
custom Header implement stretch, wave, or staged animations without changing gesture handling.

## Motion configuration

- `triggerDistance`: visible distance required to refresh; defaults to `96.dp`.
- `maxPullDistance`: asymptotic maximum visible pull; defaults to 2.5 times the trigger distance.
- `dragRate`: finger-distance ratio before the refresh threshold; defaults to `0.70f`. Progressive
  resistance starts only after the threshold so normal pulling stays responsive.
- `contentScrollableWhileRefreshing`: allows an upward gesture to close the Header and continue
  scrolling content while the refresh task runs; defaults to `true`.
- `finishDelayMillis`: how long complete/failed state remains visible; defaults to `300` ms.
- `reboundSpec`: animation used to close the Header.

Closing the Header does not cancel an active refresh. Pulling down again while it is refreshing only
reveals the same `Refreshing` Header and never invokes `onRefresh` a second time.

The content must participate in Compose nested scrolling, such as `LazyColumn`, `verticalScroll`,
or another nested-scroll child. A completely non-scrollable child does not dispatch drag deltas to
the parent. Disable the child's own vertical overscroll effect when possible so it does not render a
second edge response underneath the pull-to-refresh interaction.
