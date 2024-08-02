package com.analogics.networkservicecore.nComponent




import retrofit2.Response
import retrofit2.http.GET


interface IAPIService {
    @GET("api/?results=10")
    suspend fun getUserData(): Response<String>


}