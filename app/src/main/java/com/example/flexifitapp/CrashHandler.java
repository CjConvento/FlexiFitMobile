package com.example.flexifitapp;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Activity activity;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    public CrashHandler(Activity activity) {
        this.activity = activity;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String stack = getFullStackTrace(e);
        Log.e("APP_CRASH", stack);

        try {
            Intent intent = new Intent(activity, CrashScreenActivity.class);
            intent.putExtra("crash_stack", stack);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        } catch (Exception ex) {
            // fallback: let default handler show system crash dialog
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(t, e);
                return;
            }
        }

        // Kill app after launching crash screen
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    // ✅ Full stack trace including chained causes (no "12 more")
    private String getFullStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();

        while (throwable != null) {
            sb.append("Exception: ").append(throwable).append("\n");

            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("    at ").append(element).append("\n");
            }

            throwable = throwable.getCause();
            if (throwable != null) {
                sb.append("\nCaused by:\n");
            }
        }

        return sb.toString();
    }
}