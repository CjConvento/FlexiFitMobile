package com.example.flexifitapp;

import android.app.Application;
import android.util.Log;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class FlexiFitApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // ✅ Initialize WorkManager with a default configuration
        WorkManager.initialize(this, new Configuration.Builder().build());

        // Initialize ApiClient with application context
        ApiClient.INSTANCE.init(this);

        // Ensure Firebase is initialized (optional, usually automatic)
        FirebaseApp.initializeApp(this);

        // Optional: verify Firebase user restoration (for debugging)
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            Log.d("FlexiFitApp", "Firebase user restored: " + firebaseAuth.getCurrentUser().getUid());
        } else {
            Log.d("FlexiFitApp", "No Firebase user after app start");
        }

        // Set global uncaught exception handler (optional)
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
    }
}