package com.example.dehaatauthsdk

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

abstract class BaseHttpClient {
    private val cacheSize = 10 * 1024 * 1024 // 10 MB
    private val cache = Cache(DehaatSDKApp.instance.cacheDir, cacheSize.toLong())

    protected abstract fun createRequest(request: Request, httpUrl: HttpUrl): Request

    fun create(): OkHttpClient =
        OkHttpClient
            .Builder()
            .cache(cache)
            .addInterceptor(provideOfflineCacheInterceptor())
            .retryOnConnectionFailure(true)
            .addInterceptor(urlInterceptor())
            .let { addLogInterceptor(it).build() }

    protected fun createRequestBuilder(request: Request): Request.Builder =
        request
            .newBuilder()
            .method(request.method(), request.body())

    private fun provideOfflineCacheInterceptor() =
        Interceptor { chain ->
            try {
                chain.proceed(chain.request())
            } catch (e: Exception) {
                val cacheControl = CacheControl.Builder()
                    .onlyIfCached()
                    .maxStale(1, TimeUnit.DAYS)
                    .build()
                chain.proceed(createOfflineRequest(chain, cacheControl))
            }
        }

    private fun createOfflineRequest(chain: Interceptor.Chain, cacheControl: CacheControl) =
        chain
            .request()
            .newBuilder()
            .cacheControl(cacheControl)
            .build()

    private fun urlInterceptor() =
        Interceptor { chain ->
            val originalRequest = chain.request()
            val originalHttpUrl = originalRequest.url()
            val request = createRequest(originalRequest, originalHttpUrl)
            chain.proceed(request)
        }

    private fun addLogInterceptor(okHttpClient: OkHttpClient.Builder): OkHttpClient.Builder {
        val loggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        if (com.example.dehaatauthsdk.BuildConfig.DEBUG)
            okHttpClient.addInterceptor(loggingInterceptor)
        return okHttpClient
    }
}