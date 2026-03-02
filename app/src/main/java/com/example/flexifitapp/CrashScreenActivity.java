package com.example.flexifitapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CrashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        String rawStack = getIntent().getStringExtra("crash_stack");
        final String stack = (rawStack == null) ? "No stack trace found." : rawStack;

        TextView tv = findViewById(R.id.tvCrash);
        tv.setText(stack);

        Button btnCopy = findViewById(R.id.btnCopyCrash);
        btnCopy.setOnClickListener(v -> {
            ClipboardManager cb =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cb.setPrimaryClip(ClipData.newPlainText("crash", stack));
        });

        Button btnClose = findViewById(R.id.btnCloseApp);
        btnClose.setOnClickListener(v -> finish());
    }
}