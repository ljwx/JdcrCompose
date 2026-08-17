package com.jdcr.jdcrcompose.data

import androidx.paging.PagingSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProductPagingSourceTest {
    @Test
    fun refreshLoadsFirstPage() = runBlocking {
        val source = ProductPagingSource(
            products = ProductCatalog.products,
            loadDelayMillis = 0,
        )

        val result = source.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = PRODUCT_PAGE_SIZE,
                placeholdersEnabled = false,
            ),
        ) as PagingSource.LoadResult.Page

        assertEquals(ProductCatalog.products.take(PRODUCT_PAGE_SIZE), result.data)
        assertNull(result.prevKey)
        assertEquals(PRODUCT_PAGE_SIZE, result.nextKey)
    }

    @Test
    fun lastPageHasNoNextKey() = runBlocking {
        val source = ProductPagingSource(
            products = ProductCatalog.products,
            loadDelayMillis = 0,
        )

        val result = source.load(
            PagingSource.LoadParams.Append(
                key = 15,
                loadSize = PRODUCT_PAGE_SIZE,
                placeholdersEnabled = false,
            ),
        ) as PagingSource.LoadResult.Page

        assertEquals(ProductCatalog.products.takeLast(PRODUCT_PAGE_SIZE), result.data)
        assertEquals(10, result.prevKey)
        assertNull(result.nextKey)
    }
}
