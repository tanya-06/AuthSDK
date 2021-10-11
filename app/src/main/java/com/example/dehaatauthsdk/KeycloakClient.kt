package com.example.dehaatauthsdk

import okhttp3.HttpUrl
import okhttp3.Request

class KeycloakClient : BaseHttpClient() {

    override fun createRequest(request: Request, httpUrl: HttpUrl): Request =
        createRequestBuilder(request)
            .header("Cache-Control","no-cache")
//            .header("APP-CODE", "dehaat_business")
//            .header("Authorization", "Bearer ${AppPreference.getString(AppPreference.AuthToken)}")
            .build()
}