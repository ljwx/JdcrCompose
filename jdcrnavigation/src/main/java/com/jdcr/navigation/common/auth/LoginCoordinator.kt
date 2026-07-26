package com.jdcr.navigation.common.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface AuthLoginState {
    object Idle : AuthLoginState
    object Loading : AuthLoginState
    object Success : AuthLoginState
    data class Error(val throwable: Throwable) : AuthLoginState
}

internal class LoginMainCoordinator(private val authService: AuthService): ViewModel() {

    private val mutableState: MutableStateFlow<AuthLoginState> = MutableStateFlow(AuthLoginState.Idle)
    val state: StateFlow<AuthLoginState> = mutableState.asStateFlow()
    private var loginJob: Job? = null

    fun login(data: AuthLoginType) {
        if (loginJob?.isActive == true || state.value is AuthLoginState.Success) return

        loginJob = viewModelScope.launch {
            mutableState.value = AuthLoginState.Loading

            val result = try {
                authService.login(data)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                Result.failure(throwable)
            }

            result.fold(
                onSuccess = {
                    mutableState.value = AuthLoginState.Success
                },
                onFailure = {
                    mutableState.value = AuthLoginState.Error(it)
                },
            )
        }
    }

}