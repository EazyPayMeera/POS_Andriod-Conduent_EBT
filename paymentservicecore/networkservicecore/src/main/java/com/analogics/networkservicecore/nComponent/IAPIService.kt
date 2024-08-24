package com.analogics.networkservicecore.nComponent




import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.GET


interface IAPIService {
    @GET("api/v1/employees")
    suspend fun getEmployeeDetails(): Response<ResponseBody>

}