package com.jdcr.jdcrcompose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdcr.jdcrcompose.auth.DemoAuthService
import com.jdcr.jdcrcompose.navigation.DemoSplashInitializer
import com.jdcr.navigation.command.ExternalNavigationDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DemoAppViewModel : ViewModel() {
    val authService = DemoAuthService()
    val splashInitializer = DemoSplashInitializer()
    val externalNavigationDispatcher = ExternalNavigationDispatcher()

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

    suspend fun refreshProducts() {
        delay(2_000L)
    }
}
