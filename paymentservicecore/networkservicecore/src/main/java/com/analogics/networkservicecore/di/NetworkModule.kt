package com.analogics.networkservicecore.di


import com.analogics.networkservicecore.data.remote.IAPIService
import com.analogics.networkservicecore.data.serviceutils.NetworkConstants
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
class NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofitAPIService(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // Set connection timeout
            .readTimeout(30, TimeUnit.SECONDS)     // Set read timeout
            .writeTimeout(30, TimeUnit.SECONDS)    // Set write timeout
            .build()
        return Retrofit.Builder()
            .baseUrl(NetworkConstants.BASEURL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): IAPIService {
        return retrofit.create(IAPIService::class.java)
    }
}