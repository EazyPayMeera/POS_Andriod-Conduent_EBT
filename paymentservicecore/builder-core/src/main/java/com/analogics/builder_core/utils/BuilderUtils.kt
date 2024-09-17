package com.analogics.builder_core.utils

import com.example.example.ObjEmployeeResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class BuilderUtils @Inject constructor() {

    fun formatedGsonObject(response: String):Any
    {
        return Gson().fromJson(response,
            Any::class.java)
    }

     fun prepareAPIRequestBody(requestObj:Any): RequestBody {
        return Gson().toJson(requestObj).toByteArray()
            .toRequestBody("application/json".toMediaTypeOrNull())
    }
}