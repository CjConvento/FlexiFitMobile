package com.example.flexifitapp.utils

import android.util.Log

object AppLogger {
    // TINANGGAL ANG BUILDConfig.DEBUG CHECK
    fun d(tag: String, msg: String) {
        Log.d("FlexiFit-$tag", msg)
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (tr != null) Log.e("FlexiFit-$tag", msg, tr)
        else Log.e("FlexiFit-$tag", msg)
    }

    fun i(tag: String, msg: String) {
        Log.i("FlexiFit-$tag", msg)
    }

    fun w(tag: String, msg: String) {
        Log.w("FlexiFit-$tag", msg)
    }
}