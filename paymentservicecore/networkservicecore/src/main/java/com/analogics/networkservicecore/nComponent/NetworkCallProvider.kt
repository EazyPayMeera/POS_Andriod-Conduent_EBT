package com.eazypaytech.networkservicecore.nComponent

import android.util.Log
import com.eazypaytech.networkservicecore.serviceutils.NetworkConstants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import java.net.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.X509TrustManager


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




    fun safeApiCall(requestBytes: ByteArray): Flow<ByteArray> = callbackFlow {
        try {
            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(
                null,
                arrayOf(object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }),
                SecureRandom()
            )

            val plainSocket = Socket()

            plainSocket.connect(
                java.net.InetSocketAddress(
                    NetworkConstants.HOST_ADDRESS,
                    NetworkConstants.HOST_PORT
                ),
                30_000
            )

            val sslSocket = sslContext.socketFactory.createSocket(
                plainSocket,
                NetworkConstants.HOST_ADDRESS,
                NetworkConstants.HOST_PORT,
                true
            ) as SSLSocket

            sslSocket.soTimeout = 0 // infinite read
            sslSocket.startHandshake()

            val output = sslSocket.outputStream
            val input = sslSocket.inputStream

            // Send the request
            val lenPrefix = byteArrayOf(
                (requestBytes.size shr 8).toByte(),
                (requestBytes.size and 0xFF).toByte()
            )
            output.write(lenPrefix + requestBytes)
            output.flush()
            Log.d("Conduent", "Request sent: ${String(requestBytes, Charsets.US_ASCII)}")

            val receivedMTIs = mutableSetOf<String>()

            while (true) {
                // Read length prefix
                val lenBytes = ByteArray(2)
                val read = input.read(lenBytes)
                if (read < 2) {
                    Log.w("Conduent", "Host closed connection")
                    break
                }

                val expectedLength =
                    ((lenBytes[0].toInt() and 0xFF) shl 8) + (lenBytes[1].toInt() and 0xFF)

                // Read full message
                val msgBuffer = ByteArray(expectedLength)
                var totalRead = 0
                while (totalRead < expectedLength) {
                    val n = input.read(msgBuffer, totalRead, expectedLength - totalRead)
                    if (n == -1) break
                    totalRead += n
                }

                val isoMessage = String(msgBuffer, Charsets.US_ASCII)
                val mti = isoMessage.take(4)
                Log.d("Conduent", "Received MTI: $mti, full message: $isoMessage")

                // Emit each MTI only once (0810 and 0800)
                if (!receivedMTIs.contains(mti) && (mti == "0810" || mti == "0800")) {
                    trySend(msgBuffer).isSuccess
                    receivedMTIs.add(mti)
                }

                // Stop after both messages are received
                if (receivedMTIs.containsAll(listOf("0810", "0800"))) break
            }

            sslSocket.close()
        } catch (e: Exception) {
            close(e) // close flow on error
        }

        awaitClose { /* Cleanup socket if needed */ }
    }
        .flowOn(Dispatchers.IO)


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