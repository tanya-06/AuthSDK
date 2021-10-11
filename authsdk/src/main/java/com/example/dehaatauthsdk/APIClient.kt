package com.example.dehaatauthsdk

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIClient {
    private lateinit var keycloakService: KeyCloakService

    fun getKeyCloakService():KeyCloakService{
        if (!this::keycloakService.isInitialized)
            keycloakService = createKeyCloakService()
        return keycloakService
    }

    private fun createKeyCloakService(): KeyCloakService {
        val okHttpClient = KeycloakClient().create()
        val retrofit = createRetrofitClient(okHttpClient)
        return retrofit.create(KeyCloakService::class.java)
    }


    private fun createRetrofitClient(okHttpClient: OkHttpClient) =
        Retrofit
            .Builder()
            .baseUrl("https://oidc.dehaat.co/")
            .addConverterFactory(converterFactory())
            .client(okHttpClient)
            .build()

    private fun converterFactory() = GsonConverterFactory.create(GsonBuilder().create())
}