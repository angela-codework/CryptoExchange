package com.demo.core.util

import com.demo.core.util.logger.AppLogger
import com.google.gson.Gson

inline fun <reified T> String?.fromJsonOrNull(gson: Gson): T? {
    if (isNullOrBlank()) return null
    return try {
        gson.fromJson(this, T::class.java)
    } catch (e: Exception) {
        AppLogger.w(message = "fromJsonOrNull failed: ${e.message}")
        null
    }
}

fun Any?.toJsonOrNull(gson: Gson): String? {
    if (this == null) return null
    return try {
        gson.toJson(this)
    } catch (e: Exception) {
        AppLogger.w(message = "toJsonOrNull failed: ${e.message}")
        null
    }
}

