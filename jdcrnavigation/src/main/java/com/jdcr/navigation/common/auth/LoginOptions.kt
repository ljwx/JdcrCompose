package com.jdcr.navigation.common.auth

import kotlinx.serialization.Serializable

/** 登录页初始展示的方式；可使用内置值，也可由 App 定义自己的稳定标识。 */
@Serializable
@JvmInline
value class LoginMethod(val value: String) {
    init {
        require(value.isNotBlank()) { "LoginMethod value must not be blank" }
    }

    companion object {
        val Phone = LoginMethod("phone")
        val Account = LoginMethod("account")
        val Social = LoginMethod("social")
    }
}

/** 打开登录页的业务原因；可使用内置值，也可由 App 定义自己的稳定标识。 */
@Serializable
@JvmInline
value class LoginReason(val value: String) {
    init {
        require(value.isNotBlank()) { "LoginReason value must not be blank" }
    }

    companion object {
        val UserInitiated = LoginReason("user_initiated")
        val ProtectedRoute = LoginReason("protected_route")
        val SessionExpired = LoginReason("session_expired")
    }
}

@Serializable
data class LoginOptions(
    val initialMethod: LoginMethod = LoginMethod.Social,
    val reason: LoginReason = LoginReason.UserInitiated,
)
