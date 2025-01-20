package com.example.example

import com.eazypaytech.builder_core.model.response.ObjEmployeeData
import com.google.gson.annotations.SerializedName


data class ObjEmployeeResponse (

    @SerializedName("status"  ) var status  : String?         = null,
    @SerializedName("data"    ) var data    : ArrayList<ObjEmployeeData> = arrayListOf(),
    @SerializedName("message" ) var message : String?         = null

)