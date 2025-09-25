package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.CrashHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Objects;

public class CrashActivity extends BaseActivity {

    private TextView tvCrashLog;
    private Button btnExit, btnRestart;
    private String crashContent = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crash);
        setTitle(R.string.error);

        tvCrashLog = findViewById(R.id.tvCrashLog);
        btnExit = findViewById(R.id.btnExit);
        btnRestart = findViewById(R.id.btnRestart);

        // 读取最新日志文件
        crashContent = readLatestCrashLog();
        tvCrashLog.setText(crashContent);

        // 退出应用
        btnExit.setOnClickListener(v -> {
            Toast.makeText(this, "应用即将退出", Toast.LENGTH_SHORT).show();
            finishAffinity(); // 关闭所有 Activity
            CrashHandler.instance.defaultHandler.uncaughtException(Thread.currentThread(), new Exception(crashContent));
        });

        // 重启应用
        btnRestart.setOnClickListener(v -> {
            Toast.makeText(this, "应用即将重启", Toast.LENGTH_SHORT).show();
            restartApp(this);
        });
    }

    /** 读取 crash 文件夹中最新的日志文件 */
    private String readLatestCrashLog() {
        return readCrashFile(new File(Objects.requireNonNull(getIntent().getStringExtra("crash_path"))));
    }
    private String readCrashFile(File file) {
        if (file == null || !file.exists()) return null;

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            Log.e("CrashService", "读取日志文件失败", e);
        }
        return sb.toString();
    }
    /** 重启应用 */
    private void restartApp(Context context) {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
        finishAffinity();
        System.exit(0);
    }
}
