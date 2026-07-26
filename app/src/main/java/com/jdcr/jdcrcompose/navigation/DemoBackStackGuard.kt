package com.jdcr.jdcrcompose.navigation

import androidx.navigation3.runtime.NavKey
import com.jdcr.navigation.BackStackGuardResult
import com.jdcr.navigation.common.CommonRoute
import com.jdcr.navigation.common.auth.AuthSessionState
import com.jdcr.navigation.common.auth.LoginReason
import com.jdcr.navigation.route.BaseAppRoute
import com.jdcr.navigation.route.RequiresLogin

fun evaluateDemoBackStack(
    backStack: List<NavKey>,
    sessionState: AuthSessionState,
    unauthenticatedReason: LoginReason,
): BackStackGuardResult {
    val top = backStack.lastOrNull()

    if (top is CommonRoute.Splash) return BackStackGuardResult.Allow

    return when (sessionState) {
        AuthSessionState.Checking -> BackStackGuardResult.Pending
        AuthSessionState.Authenticated -> BackStackGuardResult.Allow
        AuthSessionState.Unauthenticated -> {
            if (top is BaseAppRoute && top is RequiresLogin) {
                val loginReason = if (unauthenticatedReason == LoginReason.SessionExpired) {
                    LoginReason.SessionExpired
                } else {
                    LoginReason.ProtectedRoute
                }
                val loginRoute = top.toAccountLogin(loginReason)
                BackStackGuardResult.ReplaceTop(loginRoute)
            } else {
                BackStackGuardResult.Allow
            }
        }
    }
}
