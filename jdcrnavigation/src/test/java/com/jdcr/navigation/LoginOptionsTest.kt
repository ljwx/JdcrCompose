package com.jdcr.navigation

import com.jdcr.navigation.common.auth.AuthLoginType
import com.jdcr.navigation.common.auth.LoginMethod
import com.jdcr.navigation.common.auth.LoginOptions
import com.jdcr.navigation.common.auth.LoginReason
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginOptionsTest {
    @Test
    fun `App 可以定义自己的登录方式和进入原因`() {
        val options = LoginOptions(
            initialMethod = LoginMethod("passkey"),
            reason = LoginReason("account_binding"),
        )

        assertEquals("passkey", options.initialMethod.value)
        assertEquals("account_binding", options.reason.value)
    }

    @Test
    fun `App 可以提交自定义登录请求`() {
        val request: AuthLoginType = PasskeyLogin("challenge")

        assertEquals(PasskeyLogin("challenge"), request)
    }

    private data class PasskeyLogin(val challenge: String) : AuthLoginType
}
