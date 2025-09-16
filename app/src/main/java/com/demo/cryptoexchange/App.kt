package com.demo.cryptoexchange

import android.app.Application
import com.demo.logger.AppLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        AppLogger.enabled = true
    }
}