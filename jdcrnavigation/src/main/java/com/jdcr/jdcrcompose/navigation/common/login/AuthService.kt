package com.jdcr.jdcrcompose.navigation.common.login

sealed interface AuthLoginType {
    data class Account(val account: String, val password: String) : AuthLoginType
    data class Social(val token: String) : AuthLoginType
    data class SMS(val phoneNumber: String, val code: String) : AuthLoginType
}

interface AuthService {

    val isLoggedIn: Boolean

    suspend fun login(data: AuthLoginType): Result<Unit>

}