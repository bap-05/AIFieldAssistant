package com.example.aifieldassistant.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {

    @POST("v1beta/models/gemini-3.6-flash:generateContent")
    suspend fun analyzeFieldReport(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {

    private const val BASE_URL =
        "https://generativelanguage.googleapis.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)

        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)

        // Tự động thử lại nếu Gemini trả 503
        .addInterceptor { chain ->

            val request = chain.request()

            var response = chain.proceed(request)
            var retryCount = 0

            while (response.code == 503 && retryCount < 3) {

                retryCount++

                response.close()

                // 2s -> 4s -> 6s
                Thread.sleep(2000L * retryCount)

                response = chain.proceed(request)
            }

            response
        }

        .build()

    val apiService: GeminiApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(GeminiApiService::class.java)
    }
}