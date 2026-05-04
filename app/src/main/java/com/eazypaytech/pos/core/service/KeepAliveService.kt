package com.eazypaytech.pos.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.analogics.paymentservicecore.data.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.pos.domain.model.ObjRootAppPaymentDetails
import com.eazypaytech.pos.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Foreground service responsible for maintaining backend connectivity
 * and performing periodic network health checks for the POS system.
 *
 * This service ensures system reliability by:
 *
 * 🔹 Running a periodic "keep-alive" handshake request every 5 minutes
 * 🔹 Performing a scheduled sign-on request every 12 hours
 * 🔹 Triggering key exchange after successful sign-on
 * 🔹 Handling retry logic on repeated failures or timeouts
 * 🔹 Running as a foreground service to avoid system kill
 *
 * ### Core Responsibilities:
 * - Maintain active session with backend API
 * - Detect network or service failures using timeout counter
 * - Trigger recovery flow after 3 consecutive failures:
 *   → Cancel keep-alive loop
 *   → Perform sign-off
 *   → Reset connection state
 *
 * ### When This Service Runs:
 * - Started after successful merchant login / POS activation
 * - Runs continuously while POS session is active
 * - Stopped on logout, session expiry, or app termination
 *
 * ### Why Foreground Service (instead of WorkManager):
 * - Requires near real-time connectivity monitoring
 * - Needs immediate retry handling on failures (no batching/delay)
 * - Backend (ISO host) expects persistent session connectivity
 * - Avoids OS background restrictions for critical payment operations
 *
 * ### Lifecycle Behavior:
 * - Started using ACTION_START with merchant/session data
 * - Runs continuously using coroutines (IO scope)
 * - Can be stopped via ACTION_STOP or system destruction
 * - Uses START_STICKY to auto-restart if killed by system
 *
 * ### Background Jobs:
 * 1. KeepAlive Job:
 *    - Starts after 30 seconds delay
 *    - Runs every 5 minutes
 *    - Executes handshake API call (only if no request is running)
 *
 * 2. 12-Hour Job:
 *    - Runs sign-on request every 12 hours
 *    - Triggers key exchange after successful sign-on
 *
 * ### Failure Handling:
 * - Tracks consecutive handshake timeouts
 * - After MAX_TIMEOUT (3):
 *   → Cancels keep-alive loop
 *   → Performs sign-off
 *   → Resets connection state
 *   → Allows reconnection cycle to restart
 *
 * ### Foreground Notification:
 * - Displays persistent "POS Active" notification
 * - Prevents system from killing service under memory pressure
 *
 * ### Notes:
 * - Ensure network availability is checked before triggering API calls
 * - Avoid overlapping requests using `isRequestRunning` flag
 * - Service is critical for maintaining transaction readiness in POS systems
 *
 * @see ApiServiceRepository
 * @see PaymentServiceUtils
 */
@AndroidEntryPoint
class KeepAliveService : Service() {

    @Inject
    lateinit var apiServiceRepository: ApiServiceRepository

    private var keepAliveJob: Job? = null
    private var twelveHourJob: Job? = null
    private var isRequestRunning = false
    private var isMidNightRunning = false

    private var timeoutCounter = 0
    private val MAX_TIMEOUT = 3

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var paymentDetail: ObjRootAppPaymentDetails? = null

    companion object {
        const val CHANNEL_ID = "keep_alive_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_PROC_ID = "procId"
        const val EXTRA_MERCHANT_ID = "merchantId"
        const val EXTRA_TERMINAL_ID = "terminalId"
        const val EXTRA_WORK_KEY = "workKey"

        @RequiresApi(Build.VERSION_CODES.O)
        fun start(context: Context, paymentDetail: ObjRootAppPaymentDetails) {
            val intent = Intent(context, KeepAliveService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_PROC_ID, paymentDetail.procId)
                putExtra(EXTRA_MERCHANT_ID, paymentDetail.merchantId)
                putExtra(EXTRA_TERMINAL_ID, paymentDetail.terminalId)
                putExtra(EXTRA_WORK_KEY, paymentDetail.workKey)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, KeepAliveService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null) {
            Log.d("KEEP_ALIVE", "Restarted with null intent — restarting loop")
            startKeepAlive()
            startTwelveHourTask()
            return START_STICKY
        }

        when (intent.action) {

            ACTION_START -> {
                paymentDetail = ObjRootAppPaymentDetails().apply {
                    procId = intent.getStringExtra(EXTRA_PROC_ID)
                    merchantId = intent.getStringExtra(EXTRA_MERCHANT_ID)
                    terminalId = intent.getStringExtra(EXTRA_TERMINAL_ID)
                    workKey = intent.getStringExtra(EXTRA_WORK_KEY)
                }
                startKeepAlive()
                startTwelveHourTask()
            }

            ACTION_STOP -> {
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun startKeepAlive() {

        if (keepAliveJob?.isActive == true) {
            Log.d("KEEP_ALIVE", "Already running, skipping new start ✅")
            return
        }

        keepAliveJob = serviceScope.launch {

            delay(30_000) // Initial delay

            while (isActive) {

                if (!isRequestRunning) {
                    Log.d("KEEP_ALIVE", "Trigger handshake: ${System.currentTimeMillis()}")
                    keepAliveProcess()
                } else {
                    Log.d("KEEP_ALIVE", "Skipping — request still running")
                }

                delay(5 * 60 * 1000L) // 5 minutes
            }
        }
    }

    private fun startTwelveHourTask() {

        if (twelveHourJob?.isActive == true) {
            Log.d("KEEP_ALIVE", "12-hour job already running")
            return
        }

        twelveHourJob = serviceScope.launch {

            while (isActive) {
                delay(12 * 60 * 60 * 1000L) // ✅ wait 12 hours first

                if (!isMidNightRunning) {
                    Log.d("KEEP_ALIVE", "12hr sign-on trigger: ${System.currentTimeMillis()}")
                    signOnRequest()
                }
            }
        }
    }

    private suspend fun keepAliveProcess() {
        isRequestRunning = true
        apiServiceRepository.handShakeRequest(
            PaymentServiceUtils.transformObject(paymentDetail),
            object : IApiServiceResponseListener {
                override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
                    Log.d("KEEP_ALIVE", "Handshake success ✅")
                    timeoutCounter = 0   // ✅ reset on success
                    isRequestRunning = false
                }

                override fun onApiServiceError(apiServiceError: ApiServiceError) {
                    Log.e("KEEP_ALIVE", "Handshake error ❌: $apiServiceError")
                    timeoutCounter++
                    Log.e("KEEP_ALIVE", "Handshake timeout ❌")
                    if (timeoutCounter >= MAX_TIMEOUT) {
                        Log.e("KEEP_ALIVE", "🔥 3 consecutive timeouts — triggering recovery")
                        timeoutCounter = 0   // reset after action
                        serviceScope.launch {
                            resetTimer()
                        }
                    }
                    isRequestRunning = false
                }

                override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                    timeoutCounter++
                    Log.e("KEEP_ALIVE", "Handshake timeout ❌")
                    if (timeoutCounter >= MAX_TIMEOUT) {
                        Log.e("KEEP_ALIVE", "🔥 3 consecutive timeouts — triggering recovery")
                        timeoutCounter = 0   // reset after action
                        serviceScope.launch {
                            resetTimer()
                        }
                    }
                    isRequestRunning = false
                }

                override fun onApiServiceDisplayProgress(
                    show: Boolean,
                    title: String?,
                    subTitle: String?,
                    message: String?
                ) {}
            }
        )
    }


    private suspend fun signOnRequest() {
        isRequestRunning = true
        isMidNightRunning = true  // ✅ add this
        apiServiceRepository.signOnRequest(
            PaymentServiceUtils.transformObject(paymentDetail),
            object : IApiServiceResponseListener {
                override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
                    isMidNightRunning = false  // ✅ reset
                    isRequestRunning = false
                    serviceScope.launch {
                        keyChange()
                    }
                }

                override fun onApiServiceError(apiServiceError: ApiServiceError) {
                    isMidNightRunning = false  // ✅ reset
                    isRequestRunning = false
                }

                override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                    isMidNightRunning = false  // ✅ reset
                    isRequestRunning = false
                }

                override fun onApiServiceDisplayProgress(
                    show: Boolean,
                    title: String?,
                    subTitle: String?,
                    message: String?
                ) {}
            }
        )
    }

    private suspend fun signOff() {
        apiServiceRepository.signOnOff(
            PaymentServiceUtils.transformObject(paymentDetail),
            object : IApiServiceResponseListener {

                override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
                    Log.d("KEEP_ALIVE", "Handshake success ✅")

                }

                override fun onApiServiceError(apiServiceError: ApiServiceError) {
                    Log.e("KEEP_ALIVE", "Handshake error ❌: $apiServiceError")

                }

                override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                    Log.e("KEEP_ALIVE", "Handshake timeout ❌")
                }

                override fun onApiServiceDisplayProgress(
                    show: Boolean,
                    title: String?,
                    subTitle: String?,
                    message: String?
                ) {}
            }
        )
    }

    private suspend fun keyChange() {
        apiServiceRepository.keyExchange(
            PaymentServiceUtils.transformObject(paymentDetail),
            object : IApiServiceResponseListener {

                override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
                    Log.d("KEEP_ALIVE", "Handshake success ✅")

                }

                override fun onApiServiceError(apiServiceError: ApiServiceError) {
                    Log.e("KEEP_ALIVE", "Handshake error ❌: $apiServiceError")

                }

                override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                    Log.e("KEEP_ALIVE", "Handshake timeout ❌")
                }

                override fun onApiServiceDisplayProgress(
                    show: Boolean,
                    title: String?,
                    subTitle: String?,
                    message: String?
                ) {}
            }
        )
    }

    private suspend fun resetTimer() {
        Log.e("KEEP_ALIVE", "Resetting KeepAlive timer + reconnect")
        keepAliveJob?.cancel()
        keepAliveJob = null
        signOff()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Keep Alive Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("POS Active")
            .setContentText("Maintaining connection...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        keepAliveJob?.cancel()
        serviceScope.cancel()
        Log.d("KEEP_ALIVE", "Service destroyed, job cancelled")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}