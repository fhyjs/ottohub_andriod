package org.eu.hanana.reimu.ottohub_andriod.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.service.CopyService;

public class DownloadTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_download_test);
        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            //Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
           // v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
           // return insets;
        //});
        findViewById(R.id.btn_test_create).setOnClickListener(v -> {
            Intent intent = new Intent(this, CopyService.class);
            intent.putExtra("uri", Uri.parse("content://org.eu.hanana.reimu.ottohub_andriod.provider.download/blog?bid=22872"));
            intent.putExtra("fileName", "blog.zip");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ 必须用 startForegroundService
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        });
    }
}