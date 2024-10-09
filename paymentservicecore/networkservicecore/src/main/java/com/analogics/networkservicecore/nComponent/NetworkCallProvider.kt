package com.analogics.networkservicecore.nComponent

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Response




object NetworkCallProvider {
    suspend fun <T>
            safeApiCall(apiCall: suspend () -> Response<T>): ResultProvider<T> {
        return try {
            val response = apiCall.invoke()
            if (response.isSuccessful) {
                ResultProvider.Success(response.body()!!)
            } else {
                ResultProvider.Error(
                    Exception(
                        response.errorBody()?.source()?.buffer?.readUtf8() ?: "Something went wrong"
                    )

                )

            }
        } catch (e: Exception) {
            Log.d("exception",e.toString())
            ResultProvider.Error(
               e
            )
//            ResultProvider.Error(e)
        }
    }


    suspend fun <T> apiCallCommon(apiCall: suspend () -> Response<T>): ResultProvider<T> {
        return try {
            val response = apiCall.invoke()
            if (response.isSuccessful) {
               // getDeserializeResponse(response.body()!!.toString(),T)
                ResultProvider.Success(response.body()!!)
               /* var userType = object: TypeToken<T>(){}.type
                var response=Gson().fromJson<T>(response.body().toString(), userType)
                ResultProvider.Success(response)*/
            } else {
                ResultProvider.Error(Exception("API call failed"))
            }
        } catch (e: Exception) {
            ResultProvider.Error(e)
        }
    }



    suspend fun <T> apiCallCommonStrng(apiCall: suspend () -> Response<T>): ResultProvider<T> {
        return try {
            val response = apiCall.invoke()
            if (response.isSuccessful) {
                // getDeserializeResponse(response.body()!!.toString(),T)
                ResultProviderString.Success(response.body().toString())
                /* var userType = object: TypeToken<T>(){}.type
                 var response=Gson().fromJson<T>(response.body().toString(), userType)
                 ResultProvider.Success(response)*/
            } else {
                ResultProviderString.Error(Exception("API call failed"))
            }
        } catch (e: Exception) {
            ResultProviderString.Error(e)
        }
    }

    fun <T : Any> getDeserializeResponse(response: String, className: T): T {
        return Gson().fromJson(
            response,
            className::class.java
        )
    }

    fun <T> stringToArray(s: String?, clazz: Class<T>?): T {
        return  Gson().fromJson(s, clazz)
        //or return Arrays.asList(new Gson().fromJson(s, clazz)); for a one-liner
    }

    fun <T> Gson.fromJson(json: String): T {
        val tt = object : TypeToken<T>() {}.type;
        println(tt);
        return fromJson(json, tt);
    }
}