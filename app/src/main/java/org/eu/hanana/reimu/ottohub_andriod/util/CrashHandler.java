package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.eu.hanana.reimu.ottohub_andriod.BuildConfig;
import org.eu.hanana.reimu.ottohub_andriod.service.CrashService;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static CrashHandler instance;
    public final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context context;

    private CrashHandler(Context context) {
        this.context = context.getApplicationContext();
        // 保存系统默认的异常处理器
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new CrashHandler(context);
            Thread.setDefaultUncaughtExceptionHandler(instance);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("bug_rep",true)){
            defaultHandler.uncaughtException(t, e);
            return;
        }
        // 1. 记录日志
        Log.e("CrashHandler", "App崩溃了: ", e);

        // 2. 这里可以把错误信息写入文件或上传服务器
        String path = saveCrashToFile(e);

        // 3. 可选：提示用户
        // 注意不能直接用 Toast，因为崩溃时主线程可能挂掉，建议用 Handler 或启动 Activity
        // showToast("应用出错了，即将重启");
        // 启动崩溃页面
        Intent serviceIntent = new Intent(context, CrashService.class);
        serviceIntent.putExtra("crash_path", path);
        ContextCompat.startForegroundService(context, serviceIntent);



        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);

    }

    private String saveCrashToFile(Throwable e) {
        String filePath = null;
        try {
            File dir = new File(context.getFilesDir(), "crash");
            if (!dir.exists()) dir.mkdirs();

            String fileName = "crash_" + System.currentTimeMillis() + ".log";
            File file = new File(dir, fileName);
            filePath = file.getAbsolutePath();

            PrintWriter pw = new PrintWriter(new FileWriter(file));

            // 写入系统信息
            pw.println("------ 系统信息 ------");
            pw.println("品牌: " + Build.BRAND);
            pw.println("型号: " + Build.MODEL);
            pw.println("Android版本: " + Build.VERSION.RELEASE);
            pw.println("SDK版本: " + Build.VERSION.SDK_INT);
            pw.println("App版本: " + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")");
            pw.println("--------------------\n");

            // 写入崩溃堆栈
            e.printStackTrace(pw);
            pw.close();

        } catch (Exception ex) {
            Log.e("CrashHandler", "保存日志失败", ex);
        }
        return filePath;
    }

    private void restartApp() {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
