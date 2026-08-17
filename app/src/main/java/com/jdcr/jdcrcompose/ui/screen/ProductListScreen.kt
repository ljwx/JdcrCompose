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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jdcr.jdcrcompose.R
import com.jdcr.jdcrcompose.data.Product
import com.jdcr.jdcrcompose.ui.component.BrandMark
import com.jdcr.jdcrcompose.ui.component.ProductArtwork
import com.jdcr.jdcrpullrefresh.JdcrClassicHeader
import com.jdcr.jdcrpullrefresh.JdcrPullRefresh
import com.jdcr.jdcrpullrefresh.PullRefreshHeaderLabels
import com.jdcr.jdcrpullrefresh.rememberPullRefreshState

@Composable
fun ProductListScreen(
    products: List<Product>,
    isLoggedIn: Boolean,
    onProductClick: (Long) -> Unit,
    onActiveLogin: () -> Unit,
    onLogout: () -> Unit,
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pullRefreshState = rememberPullRefreshState(
        onRefresh = { onRefresh() },
    )
    val headerLabels = PullRefreshHeaderLabels(
        pulling = stringResource(R.string.pull_refresh_pulling),
        ready = stringResource(R.string.pull_refresh_ready),
        refreshing = stringResource(R.string.pull_refresh_refreshing),
        complete = stringResource(R.string.pull_refresh_complete),
        failed = stringResource(R.string.pull_refresh_failed),
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
                            text = stringResource(R.string.product_count, products.size),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                items(
                    items = products,
                    key = Product::id,
                ) { product ->
                    ProductListItem(
                        product = product,
                        onClick = { onProductClick(product.id) },
                    )
                }
            }
        }
    }
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
