package com.example.flexifitapp.utils

import android.util.Log
import com.example.flexifitapp.BuildConfig

object AppLogger {
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) AppLogger.d(tag, msg)
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (tr != null) AppLogger.e(tag, msg, tr) else AppLogger.e(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (BuildConfig.DEBUG) AppLogger.i(tag, msg)
    }

    fun w(tag: String, msg: String) {
        if (BuildConfig.DEBUG) AppLogger.w(tag, msg)
    }
}