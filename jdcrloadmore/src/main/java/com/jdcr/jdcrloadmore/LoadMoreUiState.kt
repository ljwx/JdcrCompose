package com.jdcr.jdcrloadmore

import androidx.paging.LoadState

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
