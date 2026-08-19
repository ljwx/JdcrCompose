package com.jdcr.jdcrloadmore

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

/** 加载更多 Footer 可以展示的界面状态。 */
sealed interface LoadMoreUiState {
    /** 当前无需展示 Footer。 */
    data object Idle : LoadMoreUiState

    /** 正在追加下一页数据。 */
    data object Loading : LoadMoreUiState

    /** 追加数据失败，保留原始异常供自定义 Footer 使用。 */
    data class Error(val cause: Throwable) : LoadMoreUiState

    /** 已到达数据末尾。 */
    data object End : LoadMoreUiState
}

/**
 * 将 Paging 3 的追加状态转换为通用 Footer 状态。
 *
 * 该方法不依赖具体 Lazy 容器，可用于 `LazyColumn`、网格、瀑布流或业务自定义布局。
 */
fun LazyPagingItems<*>.loadMoreUiState(
    showEndOfPagination: Boolean = true,
): LoadMoreUiState = mapAppendLoadState(
    appendLoadState = loadState.append,
    itemCount = itemCount,
    showEndOfPagination = showEndOfPagination,
)

internal fun mapAppendLoadState(
    appendLoadState: LoadState,
    itemCount: Int,
    showEndOfPagination: Boolean,
): LoadMoreUiState = when (appendLoadState) {
    LoadState.Loading -> LoadMoreUiState.Loading
    is LoadState.Error -> LoadMoreUiState.Error(appendLoadState.error)
    is LoadState.NotLoading -> {
        if (appendLoadState.endOfPaginationReached &&
            itemCount > 0 &&
            showEndOfPagination
        ) {
            LoadMoreUiState.End
        } else {
            LoadMoreUiState.Idle
        }
    }
}
