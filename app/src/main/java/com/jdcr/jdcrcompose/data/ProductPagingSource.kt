package com.jdcr.jdcrcompose.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay

internal const val PRODUCT_PAGE_SIZE = 5

/** 示例商品的内存分页数据源，实际项目中通常替换为接口或数据库实现。 */
internal class ProductPagingSource(
    private val products: List<Product>,
    private val pageSize: Int = PRODUCT_PAGE_SIZE,
    private val loadDelayMillis: Long = 600L,
) : PagingSource<Int, Product>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        if (loadDelayMillis > 0) delay(loadDelayMillis)

        val startIndex = params.key ?: 0
        if (startIndex >= products.size) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = if (startIndex == 0) {
                    null
                } else {
                    (startIndex - params.loadSize).coerceAtLeast(0)
                },
                nextKey = null,
            )
        }

        val endIndex = (startIndex + params.loadSize).coerceAtMost(products.size)
        return LoadResult.Page(
            data = products.subList(startIndex, endIndex),
            prevKey = if (startIndex == 0) {
                null
            } else {
                (startIndex - params.loadSize).coerceAtLeast(0)
            },
            nextKey = endIndex.takeIf { it < products.size },
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Product>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(pageSize) ?: anchorPage.nextKey?.minus(pageSize)
    }
}
