package com.example.flexifitapp;

import android.app.Application;

public class FlexiFitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize ApiClient with application context
        ApiClient.INSTANCE.init(this);

        Thread.setDefaultUncaughtExceptionHandler(
                new CrashHandler(this)
        );
    }
}