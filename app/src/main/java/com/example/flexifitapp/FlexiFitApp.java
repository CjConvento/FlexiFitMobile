package com.example.flexifitapp;

import android.app.Application;

public class FlexiFitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(
                new CrashHandler(this)
        );
    }
}