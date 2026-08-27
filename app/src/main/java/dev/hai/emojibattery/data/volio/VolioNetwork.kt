package dev.hai.emojibattery.data.volio

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object VolioNetwork {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("User-Agent", "EmojiBatteryPort/0.1 (Android)")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: VolioStoreApi = Retrofit.Builder()
        .baseUrl(VolioConstants.PUBLIC_API_BASE)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(VolioStoreApi::class.java)
}
