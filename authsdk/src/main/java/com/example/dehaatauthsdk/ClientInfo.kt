package com.example.dehaatauthsdk

object ClientInfo {
    private lateinit var deHaatAuth: DeHaatAuth

    fun setAuthSDK(deHaatAuth: DeHaatAuth) {
        this.deHaatAuth = deHaatAuth
    }

    fun getAuthSDK() = deHaatAuth
}