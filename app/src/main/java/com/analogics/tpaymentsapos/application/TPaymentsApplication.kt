package com.analogics.tpaymentsapos.application

import android.app.Application

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TPaymentsApplication:Application() {
    override fun onCreate() {
        super.onCreate()
    }
}