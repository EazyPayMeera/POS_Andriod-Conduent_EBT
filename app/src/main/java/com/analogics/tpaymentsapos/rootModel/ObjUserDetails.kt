package com.analogics.tpaymentsapos.rootModel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ObjUserDetails(@SerializedName("userId") var userId: String? = "1234",
                          @SerializedName("userRole") var userRole: String? = "Admin",
                          @SerializedName("password") var password: String? = "123456",):Serializable
