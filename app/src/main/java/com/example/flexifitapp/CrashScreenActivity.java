package com.example.flexifitapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CrashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        TextView tvCrash = findViewById(R.id.tvCrash);
        TextView tvDeviceInfo = findViewById(R.id.tvCrashDeviceInfo);

        String rawStack = getIntent().getStringExtra("crash_stack");
        final String stack = (rawStack == null) ? "No stack trace found." : rawStack;

        tvCrash.setText(stack);

        // Device info
        String info =
                "App: FlexiFit v1.0\n" +
                        "Android: " + Build.VERSION.RELEASE + "\n" +
                        "Device: " + Build.MANUFACTURER + " " + Build.MODEL;

        tvDeviceInfo.setText(info);

        Button btnCopy = findViewById(R.id.btnCopyCrash);

        btnCopy.setOnClickListener(v -> {

            ClipboardManager cb =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            cb.setPrimaryClip(
                    ClipData.newPlainText("crash_log", stack)
            );

            Toast.makeText(this,"Crash log copied",Toast.LENGTH_SHORT).show();
        });

        Button btnClose = findViewById(R.id.btnCloseApp);

        btnClose.setOnClickListener(v -> {
            finishAffinity();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        });
    }
}