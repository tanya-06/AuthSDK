package com.example.dehaatauthsdk

import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface KeyCloakService {

    @GET
    fun getDataFromDiscoveryUrl(@Url discoveryUrl: String): Call<JSONObject>

}