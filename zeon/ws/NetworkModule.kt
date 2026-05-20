package com.example.zeon.ws

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    const val BASE_URL = "https://zeon-web-server.onrender.com/"

    private lateinit var sessionCookieJar: SessionCookieJar

    /**
     * Initialize NetworkModule with application context.
     * Must be called before any API calls (preferably in Application.onCreate())
     */
    fun initialize(context: Context) {
        sessionCookieJar = SessionCookieJar(context)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient
        get() = OkHttpClient.Builder()
            .cookieJar(sessionCookieJar)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val zeonApiService: ZeonApiService
        get() = retrofit.create(ZeonApiService::class.java)
}