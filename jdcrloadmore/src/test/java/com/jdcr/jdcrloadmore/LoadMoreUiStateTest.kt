package com.jdcr.jdcrloadmore

import androidx.paging.LoadState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class LoadMoreUiStateTest {
    @Test
    fun appendLoadingMapsToLoading() {
        val result = mapAppendLoadState(
            appendLoadState = LoadState.Loading,
            itemCount = 10,
            showEndOfPagination = true,
        )

        assertSame(LoadMoreUiState.Loading, result)
    }

    @Test
    fun appendErrorKeepsOriginalCause() {
        val cause = IllegalStateException("加载失败")

        val result = mapAppendLoadState(
            appendLoadState = LoadState.Error(cause),
            itemCount = 10,
            showEndOfPagination = true,
        )

        assertSame(cause, (result as LoadMoreUiState.Error).cause)
    }

    @Test
    fun unfinishedAppendStaysIdle() {
        val result = mapAppendLoadState(
            appendLoadState = LoadState.NotLoading(endOfPaginationReached = false),
            itemCount = 10,
            showEndOfPagination = true,
        )

        assertSame(LoadMoreUiState.Idle, result)
    }

    @Test
    fun completedNonEmptyListShowsEnd() {
        val result = mapAppendLoadState(
            appendLoadState = LoadState.NotLoading(endOfPaginationReached = true),
            itemCount = 10,
            showEndOfPagination = true,
        )

        assertSame(LoadMoreUiState.End, result)
    }

    @Test
    fun emptyListDoesNotShowEndFooter() {
        val result = mapAppendLoadState(
            appendLoadState = LoadState.NotLoading(endOfPaginationReached = true),
            itemCount = 0,
            showEndOfPagination = true,
        )

        assertSame(LoadMoreUiState.Idle, result)
    }

    @Test
    fun endFooterCanBeDisabled() {
        val result = mapAppendLoadState(
            appendLoadState = LoadState.NotLoading(endOfPaginationReached = true),
            itemCount = 10,
            showEndOfPagination = false,
        )

        assertSame(LoadMoreUiState.Idle, result)
    }

    @Test
    fun retryOnlyRunsForErrorState() {
        var retryCount = 0
        val cause = IllegalStateException("加载失败")
        val errorScope = LoadMoreFooterScope(LoadMoreUiState.Error(cause)) { retryCount++ }
        val idleScope = LoadMoreFooterScope(LoadMoreUiState.Idle) { retryCount++ }

        errorScope.retry()
        idleScope.retry()

        assertEquals(1, retryCount)
        assertSame(cause, errorScope.error)
    }
}
