package com.jdcr.jdcrcompose.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.jdcr.jdcrcompose.R
import com.jdcr.jdcrcompose.data.Product
import com.jdcr.jdcrcompose.ui.component.BrandMark
import com.jdcr.jdcrcompose.ui.component.ProductArtwork
import com.jdcr.jdcrloadmore.JdcrClassicLoadMoreFooter
import com.jdcr.jdcrloadmore.LoadMoreFooterLabels
import com.jdcr.jdcrloadmore.pagingLoadMoreFooter
import com.jdcr.jdcrpullrefresh.JdcrClassicHeader
import com.jdcr.jdcrpullrefresh.JdcrPullRefresh
import com.jdcr.jdcrpullrefresh.PullRefreshHeaderLabels
import com.jdcr.jdcrpullrefresh.PullRefreshResult
import com.jdcr.jdcrpullrefresh.rememberPullRefreshState
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

@Composable
fun ProductListScreen(
    products: LazyPagingItems<Product>,
    isLoggedIn: Boolean,
    onProductClick: (Long) -> Unit,
    onActiveLogin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pullRefreshState = rememberPullRefreshState(
        onRefresh = {
            val refreshLoadState = products.refreshAndAwaitResult()
            finish(
                if (refreshLoadState is LoadState.Error) {
                    PullRefreshResult.Failure
                } else {
                    PullRefreshResult.Success
                },
            )
        },
    )
    val headerLabels = PullRefreshHeaderLabels(
        pulling = stringResource(R.string.pull_refresh_pulling),
        ready = stringResource(R.string.pull_refresh_ready),
        refreshing = stringResource(R.string.pull_refresh_refreshing),
        complete = stringResource(R.string.pull_refresh_complete),
        failed = stringResource(R.string.pull_refresh_failed),
    )
    val loadMoreLabels = LoadMoreFooterLabels(
        loading = stringResource(R.string.load_more_loading),
        failed = stringResource(R.string.load_more_failed),
        retry = stringResource(R.string.retry),
        end = stringResource(R.string.load_more_end),
    )

    Surface(modifier = modifier.fillMaxSize()) {
        JdcrPullRefresh(
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            header = {
                JdcrClassicHeader(labels = headerLabels)
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                overscrollEffect = null,
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 32.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BrandMark()
                        TextButton(
                            onClick = if (isLoggedIn) onLogout else onActiveLogin,
                        ) {
                            Text(
                                text = stringResource(
                                    if (isLoggedIn) R.string.logout else R.string.active_login,
                                ),
                            )
                        }
                    }
                    Spacer(Modifier.height(42.dp))
                    Text(
                        text = stringResource(R.string.collection_label),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.product_list_title),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.product_list_subtitle),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = stringResource(R.string.product_count, products.itemCount),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                when {
                    products.loadState.refresh is LoadState.Loading && products.itemCount == 0 -> {
                        item(key = "initial-loading") {
                            InitialLoadState(
                                message = stringResource(R.string.product_loading),
                                showProgress = true,
                            )
                        }
                    }

                    products.loadState.refresh is LoadState.Error && products.itemCount == 0 -> {
                        item(key = "initial-error") {
                            InitialLoadState(
                                message = stringResource(R.string.product_load_failed),
                                actionLabel = stringResource(R.string.retry),
                                onAction = products::retry,
                            )
                        }
                    }

                    products.loadState.refresh is LoadState.NotLoading && products.itemCount == 0 -> {
                        item(key = "empty") {
                            InitialLoadState(
                                message = stringResource(R.string.product_empty),
                            )
                        }
                    }

                    else -> {
                        items(
                            count = products.itemCount,
                            key = products.itemKey(Product::id),
                        ) { index ->
                            products[index]?.let { product ->
                                ProductListItem(
                                    product = product,
                                    onClick = { onProductClick(product.id) },
                                )
                            }
                        }
                        pagingLoadMoreFooter(products) {
                            JdcrClassicLoadMoreFooter(labels = loadMoreLabels)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InitialLoadState(
    message: String,
    showProgress: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showProgress) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

/** 在触发刷新前订阅状态，避免遗漏一次很快的 Loading 状态变化。 */
private suspend fun LazyPagingItems<*>.refreshAndAwaitResult(): LoadState = coroutineScope {
    if (loadState.refresh is LoadState.Loading) {
        return@coroutineScope snapshotFlow { loadState.refresh }
            .first { it !is LoadState.Loading }
    }

    var loadingObserved = false
    val result = async(start = CoroutineStart.UNDISPATCHED) {
        snapshotFlow { loadState.refresh }
            .first { state ->
                when (state) {
                    LoadState.Loading -> {
                        loadingObserved = true
                        false
                    }

                    is LoadState.Error,
                    is LoadState.NotLoading,
                    -> loadingObserved
                }
            }
    }
    refresh()
    result.await()
}

@Composable
private fun ProductListItem(
    product: Product,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductArtwork(
                product = product,
                modifier = Modifier.size(92.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = product.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = product.summary,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            R.string.product_price,
                            product.priceInCents / 100,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.product_rating, product.rating),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
