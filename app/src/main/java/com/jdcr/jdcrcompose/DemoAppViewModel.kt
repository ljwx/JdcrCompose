package com.jdcr.jdcrcompose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.jdcr.jdcrcompose.auth.DemoAuthService
import com.jdcr.jdcrcompose.data.PRODUCT_PAGE_SIZE
import com.jdcr.jdcrcompose.data.ProductCatalog
import com.jdcr.jdcrcompose.data.ProductPagingSource
import com.jdcr.jdcrcompose.navigation.DemoSplashInitializer
import com.jdcr.navigation.command.ExternalNavigationDispatcher
import kotlinx.coroutines.launch

class DemoAppViewModel : ViewModel() {
    val authService = DemoAuthService()
    val splashInitializer = DemoSplashInitializer()
    val externalNavigationDispatcher = ExternalNavigationDispatcher()
    val products = Pager(
        config = PagingConfig(
            pageSize = PRODUCT_PAGE_SIZE,
            initialLoadSize = PRODUCT_PAGE_SIZE,
            prefetchDistance = 1,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = {
            ProductPagingSource(ProductCatalog.products)
        },
    ).flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            authService.restoreSession()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.logout()
        }
    }

    fun expireSession() {
        authService.expireSession()
    }
}
