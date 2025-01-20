package com.eazypaytech.posafrica.rootModel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ObjUserDetails(@SerializedName("userId") var userId: String? = null,
                          @SerializedName("userType") var userType: UserType? = UserType.CLERK,
                          @SerializedName("password") var password: String? = null):Serializable
