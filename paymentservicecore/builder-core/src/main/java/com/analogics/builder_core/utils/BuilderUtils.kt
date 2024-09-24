package com.analogics.builder_core.utils

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object BuilderUtils  {
     fun prepareAPIRequestBody(requestObj:Any): RequestBody {
        return Gson().toJson(requestObj).toByteArray()
            .toRequestBody("application/json".toMediaTypeOrNull())
    }
}