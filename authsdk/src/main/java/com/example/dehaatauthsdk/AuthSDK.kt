package com.example.dehaatauthsdk

import android.content.Context
import android.content.Intent

object AuthSDK {

    private var userName = "harsh.vardhan@agrevolution.in"
    private var password = "admin"
    private lateinit var refreshToken: String
    private lateinit var idToken: String
    private lateinit var operationState: OperationState
    private lateinit var clientId: String

    enum class OperationState {
        MOBILE_LOGIN, EMAIL_LOGIN, RENEW_TOKEN, LOGOUT
    }

    private lateinit var loginResponseCallback: LoginResponseCallback

    private lateinit var logoutResponseCallback: LogoutCallback

    fun getUserName() = userName

    fun getPassword() = password

    fun getLoginCallback() = loginResponseCallback

    fun getLogoutCallback() = logoutResponseCallback

    fun getOperationState() = operationState

    fun getRefreshToken() = refreshToken

    fun getIdToken() = idToken

    fun getClientId() = clientId

    fun setUserName(userName: String){
        this.userName = userName
    }

    fun setPassword(password: String){
        this.password = password
    }

    fun setClientId(clientId: String){
        this.clientId = clientId
    }

    fun setLoginResponseCallback(callback: LoginResponseCallback) {
        loginResponseCallback = callback
    }

    fun setLogoutResponseCallback(callback: LogoutCallback) {
        logoutResponseCallback = callback
    }

    fun loginWithMobile(context: Context, mobile: String, otp: String) {
        this.operationState = OperationState.MOBILE_LOGIN
        this.userName = mobile
        this.password = otp
        context.startActivity(Intent(context, LoginActivity::class.java))
    }

    fun loginWithEmail(context: Context, userName: String, password: String) {

    }

    fun renewToken(context: Context, refreshToken: String) {
        this.refreshToken = refreshToken
        this.operationState = OperationState.RENEW_TOKEN
        context.startActivity(Intent(context, LoginActivity::class.java))
    }

    fun logout(context: Context, idToken: String) {
        this.operationState = OperationState.LOGOUT
        this.idToken = idToken
        context.startActivity(Intent(context, LoginActivity::class.java))
    }
}