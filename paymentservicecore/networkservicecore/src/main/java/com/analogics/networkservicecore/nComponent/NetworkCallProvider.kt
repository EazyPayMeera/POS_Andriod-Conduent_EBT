package com.eazypaytech.networkservicecore.nComponent

import android.util.Log
import com.eazypaytech.networkservicecore.serviceutils.NetworkConstants
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
import kotlinx.coroutines.withTimeout
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.CoroutineContext


object NetworkCallProvider {
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ResultProvider<T> {
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

//    suspend fun safeApiCall(request: ByteArray): ResultProvider<ByteArray> {
//        return try {
//            withContext(Dispatchers.IO)
//            {
//                val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
//                    .connect(InetSocketAddress(NetworkConstants.HOST_ADDRESS, NetworkConstants.HOST_PORT))
//                val input = socket.openReadChannel()
//                val output = socket.openWriteChannel(autoFlush = true)
//                var response = ByteArray(0)
//                var packetLength : Int = 0
//                output.writeFully(request)
//                do {
//                    delay(100)
//                    if (input.isClosedForRead) break
//                    if(input.availableForRead>0) {
//                        var chunk = ByteArray(input.availableForRead)
//                        input.readAvailable(chunk, 0, chunk.size)
//                        response += chunk
//                        if(response.size>=2)
//                            packetLength = (response[0] * 256) + (response[1] % 256)
//                    }
//                }while (input.isClosedForRead.not() && (packetLength==0 || response.size<packetLength))
//                socket.close()
//
//                if(response.isNotEmpty())
//                    ResultProvider.Success(response)
//                else
//                    ResultProvider.Error(Exception("No Response"))
//            }
//        } catch (e: Exception) {
//            Log.d("exception",e.toString())
//            ResultProvider.Error(
//                e
//            )
//        }
//    }



    suspend fun safeApiCall(requestBytes: ByteArray): ResultProvider<ByteArray> {
        return try {
            withContext(Dispatchers.IO) {
                val sslContext = SSLContext.getInstance("TLSv1.2")
                sslContext.init(null, arrayOf(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }), SecureRandom())
                val sslSocket = sslContext.socketFactory.createSocket(
                    NetworkConstants.HOST_ADDRESS,
                    NetworkConstants.HOST_PORT
                ) as SSLSocket


                sslSocket.startHandshake()


                val output = sslSocket.outputStream
                val input = sslSocket.inputStream

                // --- Add 2-byte length prefix ---
                val lenPrefix = byteArrayOf((requestBytes.size / 256).toByte(), (requestBytes.size % 256).toByte())
                val finalMessage = lenPrefix + requestBytes
                //Log.d("Conduent", "Sending request with length ${requestBytes.size} (total bytes with prefix: ${finalMessage.size})")

                // --- Send request ---
                output.write(finalMessage)
                output.flush()
                //Log.d("Conduent", "Request sent")

                // --- Read response ---
                val responseBuffer = ByteArrayOutputStream()

                // First, read 2-byte length prefix
                val lenBytes = ByteArray(2)
                var readLen = 0
                while (readLen < 2) {
                    val n = input.read(lenBytes, readLen, 2 - readLen)
                    if (n == -1) throw Exception("No response received (length prefix)")
                    readLen += n
                }

                val expectedLength = ((lenBytes[0].toInt() and 0xFF) * 256) + (lenBytes[1].toInt() and 0xFF)
                Log.d("Conduent", "Expected response length from prefix: $expectedLength")

                // Then, read the full message based on length
                val tempBuffer = ByteArray(4096)
                var totalRead = 0
                while (totalRead < expectedLength) {
                    val toRead = minOf(tempBuffer.size, expectedLength - totalRead)
                    val n = input.read(tempBuffer, 0, toRead)
                    if (n == -1) throw Exception("Stream closed before full response")
                    responseBuffer.write(tempBuffer, 0, n)
                    totalRead += n
                    Log.d("Conduent", "Total bytes read so far: $totalRead")
                }

                sslSocket.close()
                Log.d("Conduent", "Socket closed")

                val finalResponse = responseBuffer.toByteArray()
                Log.d("Conduent", "Final response received, total bytes: ${finalResponse.size}")
                Log.d("Conduent", "Response HEX: ${finalResponse.joinToString(" ") { "%02X".format(it) }}")
                Log.d("Conduent","Response ASCII: ${String(finalResponse, Charsets.US_ASCII)}")

                ResultProvider.Success(finalResponse)
            }
        } catch (e: Exception) {
            Log.e("Conduent", "Exception: ${e.message}")
            ResultProvider.Error(e)
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