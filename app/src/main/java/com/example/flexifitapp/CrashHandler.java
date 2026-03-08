package com.example.flexifitapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Context appContext;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    public CrashHandler(Context context) {
        this.appContext = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {

        String stack = getFullStackTrace(e);

        Log.e("APP_CRASH", stack);

        // ✅ Save crash log to file
        writeCrashToFile(stack);

        try {
            Intent intent = new Intent(appContext, CrashScreenActivity.class);
            intent.putExtra("crash_stack", stack);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);

        } catch (Exception ex) {

            if (defaultHandler != null) {
                defaultHandler.uncaughtException(t, e);
                return;
            }
        }

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {
        }

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    // ✅ Write crash log file
    private void writeCrashToFile(String stack) {

        try {

            File dir = new File(appContext.getExternalFilesDir(null), "FlexiFit");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, "crash_log.txt");

            FileWriter writer = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(writer);

            String time = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
            ).format(new Date());

            pw.println("=========== CRASH ===========");
            pw.println("Time: " + time);
            pw.println("App: FlexiFit v1.0");
            pw.println("Android: " + android.os.Build.VERSION.RELEASE);
            pw.println("Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
            pw.println();
            pw.println(stack);
            pw.println();

            pw.close();

        } catch (Exception ex) {

            Log.e("CRASH_HANDLER", "Failed to write crash log", ex);
        }
    }

    // ✅ Full stack trace
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