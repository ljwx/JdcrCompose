package com.jdcr.navigation.common.auth

import kotlinx.coroutines.flow.StateFlow

sealed interface AuthLoginType {
    data class Account(val account: String, val password: String) : AuthLoginType
    data class Social(val token: String) : AuthLoginType
    data class SMS(val phoneNumber: String, val code: String) : AuthLoginType
}

sealed interface AuthSessionState {
    data object Checking : AuthSessionState
    data object Authenticated : AuthSessionState
    data object Unauthenticated : AuthSessionState
}

interface AuthService {

    val sessionState: StateFlow<AuthSessionState>

    val isLoggedIn: Boolean
        get() = sessionState.value is AuthSessionState.Authenticated

    // 成功返回前，必须先更新 isLoggedIn。
    suspend fun login(data: AuthLoginType): Result<Unit>

    suspend fun logout(): Result<Unit>

}