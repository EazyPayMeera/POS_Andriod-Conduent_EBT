package com.analogics.tpaymentsapos.application

import android.app.Application
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TPaymentsApplication:Application() {
    override fun onCreate() {
        super.onCreate()
    }
}