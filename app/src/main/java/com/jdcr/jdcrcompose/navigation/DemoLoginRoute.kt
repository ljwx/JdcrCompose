package com.jdcr.jdcrcompose.navigation

import com.jdcr.navigation.common.CommonRoute
import com.jdcr.navigation.common.auth.LoginMethod
import com.jdcr.navigation.common.auth.LoginOptions
import com.jdcr.navigation.common.auth.LoginReason
import com.jdcr.navigation.route.BaseAppRoute

fun BaseAppRoute.toAccountLogin(
    reason: LoginReason = LoginReason.ProtectedRoute,
): CommonRoute.Login = CommonRoute.Login(
    returnTo = this,
    options = LoginOptions(
        initialMethod = LoginMethod.Account,
        reason = reason,
    ),
)
