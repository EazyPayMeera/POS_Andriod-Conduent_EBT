package com.analogics.networkservicecore.nComponent

import android.util.Log
import com.analogics.networkservicecore.serviceutils.NetworkConstants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.availableForRead
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import kotlin.coroutines.CoroutineContext


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
        }
    }

    suspend fun
            safeApiCall(request: ByteArray): ResultProvider<ByteArray> {
        return try {
            withContext(context = Dispatchers.Main)
            {
                val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                    .connect(InetSocketAddress(NetworkConstants.HOST_ADDRESS, NetworkConstants.HOST_PORT))
                val input = socket.openReadChannel()
                val output = socket.openWriteChannel(autoFlush = true)
                var response = ByteArray(0)
                var packetLength : Int = 0
                output.writeFully(request)
                do {
                    delay(100)
                    if (input.isClosedForRead) break
                    if(input.availableForRead>0) {
                        var chunk = ByteArray(input.availableForRead)
                        input.readAvailable(chunk, 0, chunk.size)
                        response += chunk
                        if(response.size>=2)
                            packetLength = (response[0] * 256) + (response[1] % 256)
                    }
                }while (input.isClosedForRead.not() && (packetLength==0 || response.size<packetLength))
                socket.close()

                if(response.isNotEmpty())
                    ResultProvider.Success(response)
                else
                    ResultProvider.Error(Exception("No Response"))
            }
        } catch (e: Exception) {
            Log.d("exception",e.toString())
            ResultProvider.Error(
                e
            )
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