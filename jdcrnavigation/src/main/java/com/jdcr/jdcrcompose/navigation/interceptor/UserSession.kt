package com.jdcr.jdcrcompose.navigation.interceptor

interface UserSession {
    val isLoggedIn: Boolean

    fun <T> loginParams(): T?

}