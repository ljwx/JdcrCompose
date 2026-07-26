package com.jdcr.navigation.common.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LoginMethod {
    @SerialName("phone") Phone,
    @SerialName("account") Account,
    @SerialName("social") Social,
}

@Serializable
enum class LoginReason {
    @SerialName("user_initiated") UserInitiated,
    @SerialName("protected_route") ProtectedRoute,
    @SerialName("session_expired") SessionExpired,
}

@Serializable
data class LoginOptions(
    val initialMethod: LoginMethod = LoginMethod.Social,
    val reason: LoginReason = LoginReason.UserInitiated,
)