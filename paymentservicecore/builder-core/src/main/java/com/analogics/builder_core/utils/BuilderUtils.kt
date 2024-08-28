package com.analogics.builder_core.utils

import com.example.example.ObjEmployeeResponse
import com.google.gson.Gson
import javax.inject.Inject

class BuilderUtils @Inject constructor() {

    fun formatedGsonObject(response: String):Any
    {
        return Gson().fromJson(response,
            ObjEmployeeResponse::class.java)
    }
}