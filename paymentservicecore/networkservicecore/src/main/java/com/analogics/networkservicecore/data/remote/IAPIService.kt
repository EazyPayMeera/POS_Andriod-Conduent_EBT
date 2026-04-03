package com.eazypaytech.networkservicecore.nComponent




import com.eazypaytech.networkservicecore.serviceutils.NetworkConstants
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST


interface IAPIService {
    @GET("api/v1/employees")
    suspend fun getEmployeeDetails(): Response<ResponseBody>

    @Headers("${NetworkConstants.KEY_X_GP_VERSION}: ${NetworkConstants.VAL_X_GP_VERSION}")
    @POST(NetworkConstants.PATH_ACCESS_TOKEN)
    suspend fun getAccessToken(@Body requestBody: RequestBody):Response<ResponseBody>
    @POST("/ucp/accesstoken")
    suspend fun login(@Body requestBody: RequestBody):Response<ResponseBody>
    @POST("/void")
    suspend fun getVoid(@Body requestBody: RequestBody):Response<ResponseBody>
    @POST("/refund")
    suspend fun getRefund(@Body requestBody: RequestBody):Response<ResponseBody>
    @POST("/purchase")
    suspend fun getPurchase(@Body requestBody: RequestBody):Response<ResponseBody>
    @POST("reversal")
    suspend fun getReversal(@Body requestBody: RequestBody):Response<ResponseBody>
    @POST("pre-auth")
    suspend fun getPreAuth(@Body requestBody: RequestBody):Response<ResponseBody>
    @POST("auth-capture")
    suspend fun getAuthCapture(@Body requestBody: RequestBody):Response<ResponseBody>
}