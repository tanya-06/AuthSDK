package com.example.dehaatauthsdk

import android.content.Context
import android.content.Intent

object AuthSDK {

    private var userName = "harsh.vardhan@agrevolution.in"
    private var password ="admin"
    private lateinit var refreshToken :String
    private lateinit var idToken :String
    private var operationState =OperationState.LOGIN

    enum class OperationState{
        LOGIN, RENEW_TOKEN, LOGOUT
    }

    private lateinit var loginResponseCallback :LoginResponseCallback

    private lateinit var logoutResponseCallback :LogoutCallback

    fun getUserName() = userName

    fun getPassword() = password

    fun getLoginCallback() = loginResponseCallback

    fun getLogoutCallback() = logoutResponseCallback

    fun getOperationState() = operationState

    fun getRefreshToken() = refreshToken

    fun getIdToken() = idToken

    fun setLoginResponseCallback(callback: LoginResponseCallback) {
        loginResponseCallback = callback
    }

    fun setLogoutResponseCallback(callback: LogoutCallback) {
        logoutResponseCallback = callback
    }

    fun doLogin(context: Context, userName: String, password: String) {
        this.operationState= OperationState.LOGIN
        this.userName = userName
        this.password = password
        context.startActivity(Intent(context, LoginActivity::class.java))
    }

    fun renewToken(context: Context,refreshToken :String){
        this.refreshToken = refreshToken
        this.operationState = OperationState.RENEW_TOKEN
        context.startActivity(Intent(context, LoginActivity::class.java))
    }


    fun logout(context: Context, idToken :String) {
        this.operationState = OperationState.LOGOUT
        this.idToken = idToken
        context.startActivity(Intent(context, LoginActivity::class.java))
    }
}