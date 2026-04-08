package com.eazypaytech.networkservicecore.nComponent

import android.util.Log
import com.eazypaytech.networkservicecore.serviceutils.NetworkConstants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.X509TrustManager


object NetworkCallProvider {

    suspend fun safeApiCall(requestBytes: ByteArray): ResultProvider<ByteArray> {
        return try {
            withContext(Dispatchers.IO) {

                val sslContext = SSLContext.getInstance("TLSv1.2")
                sslContext.init(
                    null,
                    arrayOf(object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }),
                    SecureRandom()
                )

                val plainSocket = Socket()
                plainSocket.connect(
                    java.net.InetSocketAddress(NetworkConstants.HOST_ADDRESS, NetworkConstants.HOST_PORT),
                    30_000
                )

                val sslSocket = sslContext.socketFactory
                    .createSocket(plainSocket, NetworkConstants.HOST_ADDRESS, NetworkConstants.HOST_PORT, true) as SSLSocket

                sslSocket.soTimeout = 30_000
                sslSocket.startHandshake()

                val output = sslSocket.outputStream
                val input = sslSocket.inputStream

                val lenPrefix = byteArrayOf(
                    (requestBytes.size shr 8).toByte(),
                    (requestBytes.size and 0xFF).toByte()
                )

                val fullRequest = lenPrefix + requestBytes


                output.write(fullRequest)
                output.flush()
                val lenBytes = ByteArray(2)
                input.read(lenBytes)
                val expectedLength = ((lenBytes[0].toInt() and 0xFF) shl 8) + (lenBytes[1].toInt() and 0xFF)

                val responseBuffer = ByteArrayOutputStream()
                val tempBuffer = ByteArray(4096)
                var totalRead = 0

                while (totalRead < expectedLength) {
                    val n = input.read(tempBuffer, 0, minOf(tempBuffer.size, expectedLength - totalRead))
                    if (n == -1) throw Exception("Stream closed early")
                    responseBuffer.write(tempBuffer, 0, n)
                    totalRead += n
                }

                val responseBytes = responseBuffer.toByteArray()

                sslSocket.close()

                ResultProvider.Success(responseBytes)
            }

        } catch (e: SocketTimeoutException) {
            Log.e("SOCKET", "⏱ Timeout: ${e.message}")
            ResultProvider.Error(e)

        } catch (e: Exception) {
            Log.e("SOCKET", "❌ Error: ${e.message}")
            ResultProvider.Error(e)
        }
    }

    fun safeApiResponse(requestBytes: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {
            var sslSocket: SSLSocket? = null
            try {
                val sslContext = SSLContext.getInstance("TLSv1.2")
                sslContext.init(
                    null,
                    arrayOf(object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }),
                    SecureRandom()
                )

                val plainSocket = Socket()
                plainSocket.connect(InetSocketAddress(NetworkConstants.HOST_ADDRESS, NetworkConstants.HOST_PORT), 15_000)

                sslSocket = sslContext.socketFactory.createSocket(plainSocket, NetworkConstants.HOST_ADDRESS, NetworkConstants.HOST_PORT, true) as SSLSocket
                sslSocket.startHandshake()

                val lenPrefix = byteArrayOf(
                    (requestBytes.size shr 8).toByte(),
                    (requestBytes.size and 0xFF).toByte()
                )

                sslSocket.outputStream.write(lenPrefix + requestBytes)
                sslSocket.outputStream.flush()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                sslSocket?.close()
            }
        }
    }



    fun safeApiNetworkCall(requestBytes: ByteArray): Flow<ByteArray> = callbackFlow {
        var intentionallyClosed = false  // 👈 add this flag

        try {
            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(
                null,
                arrayOf(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }),
                SecureRandom()
            )

            val plainSocket = Socket()
            plainSocket.connect(
                java.net.InetSocketAddress(NetworkConstants.HOST_ADDRESS, NetworkConstants.HOST_PORT),
                15_000
            )

            val sslSocket = sslContext.socketFactory.createSocket(
                plainSocket,
                NetworkConstants.HOST_ADDRESS,
                NetworkConstants.HOST_PORT,
                true
            ) as SSLSocket

            sslSocket.soTimeout = 15_000
            sslSocket.startHandshake()

            val output = sslSocket.outputStream
            val input = sslSocket.inputStream

            val lenPrefix = byteArrayOf(
                (requestBytes.size shr 8).toByte(),
                (requestBytes.size and 0xFF).toByte()
            )
            output.write(lenPrefix + requestBytes)
            output.flush()
            val receivedMTIs = mutableSetOf<String>()

            while (true) {
                val lenBytes = ByteArray(2)
                val read = input.read(lenBytes)
                if (read < 2) {
                    break
                }

                val expectedLength =
                    ((lenBytes[0].toInt() and 0xFF) shl 8) + (lenBytes[1].toInt() and 0xFF)

                val msgBuffer = ByteArray(expectedLength)
                var totalRead = 0
                while (totalRead < expectedLength) {
                    val n = input.read(msgBuffer, totalRead, expectedLength - totalRead)
                    if (n == -1) break
                    totalRead += n
                }

                val isoMessage = String(msgBuffer, Charsets.US_ASCII)
                val mti = isoMessage.take(4)

                if (!receivedMTIs.contains(mti) && (mti == "0810" || mti == "0800")) {
                    trySend(msgBuffer).isSuccess
                    receivedMTIs.add(mti)
                }

                if (receivedMTIs.containsAll(listOf("0800"))) {
                    intentionallyClosed = true  // 👈 set flag before closing
                    sslSocket.close()           // 👈 close socket here
                    plainSocket.close()
                    close()                     // 👈 then close flow
                    break
                }
            }

        } catch (e: Exception) {
            if (!intentionallyClosed) {  // 👈 only forward real errors
                close(e)
            }
        }

        awaitClose { }
    }.flowOn(Dispatchers.IO)

}