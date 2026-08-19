package com.jdcr.navigation.common.auth

import kotlinx.coroutines.flow.StateFlow

/**
 * 登录请求数据。库提供常用实现，App 也可以实现该接口接入通行密钥、扫码等自定义方式。
 */
interface AuthLoginType {
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
