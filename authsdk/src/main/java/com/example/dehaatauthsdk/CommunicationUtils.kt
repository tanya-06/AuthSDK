package com.example.dehaatauthsdk

import android.content.Context
import android.content.Intent

object CommunicationUtils {

    private var userName = "harsh.vardhan@agrevolution.in"
    private var password ="admin"

    private lateinit var loginResponseCallback :LoginResponseCallback

    fun getUserName() = userName

    fun getPassword() = password

    fun getLoginCallback() = loginResponseCallback

    fun setLoginResponseCallback(callback: LoginResponseCallback) {
        loginResponseCallback = callback
    }

    fun startLoginFromNative(context: Context, userName: String, password: String) {
        this.userName = userName
        this.password = password
        context.startActivity(Intent(context, LoginActivity::class.java))
    }
}