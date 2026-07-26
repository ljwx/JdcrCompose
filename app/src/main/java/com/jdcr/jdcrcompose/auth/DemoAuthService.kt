package com.jdcr.jdcrcompose.auth

import com.jdcr.navigation.common.auth.AuthLoginType
import com.jdcr.navigation.common.auth.AuthService
import com.jdcr.navigation.common.auth.AuthSessionState
import com.jdcr.navigation.common.auth.LoginReason
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DemoAuthService : AuthService {
    private val mutableSessionState =
        MutableStateFlow<AuthSessionState>(AuthSessionState.Checking)

    override val sessionState: StateFlow<AuthSessionState> =
        mutableSessionState.asStateFlow()

    private val mutableUnauthenticatedReason =
        MutableStateFlow(LoginReason.UserInitiated)

    val unauthenticatedReason: StateFlow<LoginReason> =
        mutableUnauthenticatedReason.asStateFlow()

    suspend fun restoreSession() {
        if (mutableSessionState.value !is AuthSessionState.Checking) return

        delay(300)
        mutableUnauthenticatedReason.value = LoginReason.UserInitiated
        mutableSessionState.value = AuthSessionState.Unauthenticated
    }

    override suspend fun login(data: AuthLoginType): Result<Unit> {
        delay(LOGIN_DELAY_MILLIS)

        val account = data as? AuthLoginType.Account
            ?: return Result.failure(IllegalArgumentException("当前示例仅支持账号密码登录"))

        return if (account.account == DEMO_ACCOUNT && account.password == DEMO_PASSWORD) {
            mutableSessionState.value = AuthSessionState.Authenticated
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("账号或密码错误"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        mutableUnauthenticatedReason.value = LoginReason.UserInitiated
        mutableSessionState.value = AuthSessionState.Unauthenticated
        return Result.success(Unit)
    }

    fun expireSession() {
        mutableUnauthenticatedReason.value = LoginReason.SessionExpired
        mutableSessionState.value = AuthSessionState.Unauthenticated
    }

    companion object {
        const val DEMO_ACCOUNT = "ljwx"
        const val DEMO_PASSWORD = "123456"
        private const val LOGIN_DELAY_MILLIS = 2_000L
    }
}
