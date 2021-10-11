package com.example.dehaatauthsdk

import android.app.Application

class DehaatSDKApp : Application() {


    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    companion object {
        lateinit var instance: DehaatSDKApp
    }
}