package com.analogics.networkservicecore.di

import com.analogics.networkservicecore.tms.api.TmsApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TmsNetworkModule {

    @Provides
    @Singleton
    @TmsRetrofit   // ✅ IMPORTANT
    fun provideTmsRetrofit(): Retrofit {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://unitms.morefun-et.com/") //
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTmsApiService(
        @TmsRetrofit retrofit: Retrofit   // ✅ IMPORTANT
    ): TmsApiService {
        return retrofit.create(TmsApiService::class.java)
    }
}