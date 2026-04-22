package com.analogics.networkservicecore.tms.api

import com.analogics.networkservicecore.tms.model.TmsResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TmsApiService {

    @FormUrlEncoded
    @POST("/home/api/getDeviceParam.html")
    suspend fun getDeviceParams(
        @Field("sn") sn: String,
        @Field("access_key") accessKey: String,
        @Field("sign") sign: String,
        @Field("is_full_update") isFullUpdate: Int = 1
    ): TmsResponse
}