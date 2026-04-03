package com.eazypaytech.posafrica.core.application

import android.app.Application

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TPaymentsApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}