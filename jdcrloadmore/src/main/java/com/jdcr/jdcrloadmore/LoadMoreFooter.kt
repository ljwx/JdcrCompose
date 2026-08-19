package com.jdcr.jdcrloadmore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems

/** 自定义 Footer 可读取的状态以及可执行的重试操作。 */
class LoadMoreFooterScope internal constructor(
    val state: LoadMoreUiState,
    private val onRetry: () -> Unit,
) {
    /** 当 [state] 为 [LoadMoreUiState.Error] 时返回追加失败的异常。 */
    val error: Throwable?
        get() = (state as? LoadMoreUiState.Error)?.cause

    /** 仅在追加失败时重试当前 Paging 请求。 */
    fun retry() {
        if (state is LoadMoreUiState.Error) onRetry()
    }
}

/** 默认经典 Footer 使用的文字，可由业务层替换以实现本地化。 */
@Immutable
data class LoadMoreFooterLabels(
    val loading: String = "Loading more...",
    val failed: String = "Failed to load",
    val retry: String = "Retry",
    val end: String = "No more data",
)

/**
 * 根据 [state] 展示加载更多 Footer。
 *
 * [LoadMoreUiState.Idle] 不会产生任何布局高度。自定义 [footer] 只负责展示，分页请求仍由
 * Paging 3 根据列表项访问和 `prefetchDistance` 自动触发。
 */
@Composable
fun JdcrLoadMoreFooter(
    state: LoadMoreUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    footer: @Composable LoadMoreFooterScope.() -> Unit = {
        JdcrClassicLoadMoreFooter()
    },
) {
    if (state == LoadMoreUiState.Idle) return

    val latestOnRetry = rememberUpdatedState(onRetry)
    val scope = remember(state, latestOnRetry) {
        LoadMoreFooterScope(
            state = state,
            onRetry = { latestOnRetry.value() },
        )
    }
    Box(modifier = modifier) {
        scope.footer()
    }
}

/**
 * 在 `LazyColumn` 末尾添加 Paging 3 加载更多 Footer。
 *
 * 该扩展只读取 [LazyPagingItems.loadState]，不会维护页码、发起业务请求或注册滚动监听。
 * [key] 必须在同一个列表中保持唯一；使用字符串类型可以确保它能被 Android Bundle 保存。
 */
fun LazyListScope.pagingLoadMoreFooter(
    pagingItems: LazyPagingItems<*>,
    key: String = DEFAULT_LOAD_MORE_FOOTER_KEY,
    modifier: Modifier = Modifier,
    showEndOfPagination: Boolean = true,
    footer: @Composable LoadMoreFooterScope.() -> Unit = {
        JdcrClassicLoadMoreFooter()
    },
) {
    item(
        key = key,
        contentType = LoadMoreFooterContentType,
    ) {
        val state = pagingItems.loadMoreUiState(showEndOfPagination)
        JdcrLoadMoreFooter(
            state = state,
            onRetry = pagingItems::retry,
            modifier = modifier,
            footer = footer,
        )
    }
}

/** 一个尺寸稳定、风格中性的默认加载更多 Footer。 */
@Composable
fun LoadMoreFooterScope.JdcrClassicLoadMoreFooter(
    modifier: Modifier = Modifier,
    labels: LoadMoreFooterLabels = LoadMoreFooterLabels(),
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (state) {
            LoadMoreUiState.Idle -> Unit
            LoadMoreUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = tint,
                    strokeWidth = 2.dp,
                )
                Text(
                    text = labels.loading,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            is LoadMoreUiState.Error -> {
                Text(
                    text = labels.failed,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = ::retry) {
                    Text(labels.retry)
                }
            }

            LoadMoreUiState.End -> Text(
                text = labels.end,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private const val DEFAULT_LOAD_MORE_FOOTER_KEY = "com.jdcr.jdcrloadmore.footer"

private object LoadMoreFooterContentType
