package com.demo.cryptoexchange

import android.app.Application
import com.demo.core.util.logger.AppLogger

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        AppLogger.enabled = BuildConfig.DEBUG
    }
}