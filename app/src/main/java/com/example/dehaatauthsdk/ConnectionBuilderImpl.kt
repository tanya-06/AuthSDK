package com.example.dehaatauthsdk

import android.net.Uri
import net.openid.appauth.connectivity.ConnectionBuilder
import java.net.HttpURLConnection

open class ConnectionBuilderImpl : ConnectionBuilder {

    override fun openConnection(uri: Uri): HttpURLConnection {
        TODO("Not yet implemented")
    }
}