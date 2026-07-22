package com.jdcr.jdcrcompose.navigation.common.login

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface AuthLoginState {
    object Idle : AuthLoginState
    object Loading : AuthLoginState
    object Success : AuthLoginState
    data class Error(val throwable: Throwable) : AuthLoginState
}

class LoginCoordinator(private val authService: AuthService) {

    private var data: AuthLoginType? = null
    private val mutableState: MutableStateFlow<AuthLoginState> =
        MutableStateFlow(AuthLoginState.Idle)
    val state: StateFlow<AuthLoginState> = mutableState.asStateFlow()

    fun updateLoginData(data: AuthLoginType) {
        this.data = data
    }

    suspend fun login() {
        val currentState = mutableState.value
        if (currentState is AuthLoginState.Loading) return
        val currentData = data ?: return
        mutableState.update { AuthLoginState.Loading }
        authService.login(currentData).onSuccess {
            mutableState.update { AuthLoginState.Success }
        }.onFailure { throwable ->
            mutableState.update {
                AuthLoginState.Error(throwable)
            }
        }
    }

}