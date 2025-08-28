package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageViewActivity extends BaseActivity {
    public static final String EXTRA_IMAGE_URL = "image_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_view);
        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            //Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
           // v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
           // return insets;
        //});
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.image_viewer);
        String imageUrl = getIntent().getExtras().getString(EXTRA_IMAGE_URL);
        PhotoView photoView = findViewById(R.id.photoView);
        // 可换成你用的图片库，如 Glide/Picasso 等
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.error_48px)
                .into(photoView);

        // 单击关闭
        photoView.setOnPhotoTapListener((view, x, y) -> finish());

        // 启用退出动画（可选）
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        Toast.makeText(this, R.string.image_viewer_tip, Toast.LENGTH_SHORT).show();
        photoView.setOnLongClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.image_viewer)
                    .setMessage(R.string.image_viewer_save_tip)
                    .setPositiveButton(R.string.conform, (dialog, which) -> {
                        downloadImageFromUrl(this, imageUrl); // 直接下载原始 URL
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return true;
        });
    }
    private void saveStreamToGallery(Context context, InputStream inputStream, String fileName, String mimeType) throws IOException {
        OutputStream outputStream;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ottohub");

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new IOException("无法创建媒体文件");

            outputStream = resolver.openOutputStream(uri);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(()->AlertUtil.showError(this,getString(R.string.no_permission)).show());
                return;
            }
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ottohub");
            if (!dir.exists()) dir.mkdirs();

            File imageFile = new File(dir, fileName);
            outputStream = new FileOutputStream(imageFile);

            // 通知图库
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(imageFile));
            context.sendBroadcast(intent);
        }

        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }

        outputStream.flush();
        outputStream.close();

        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, "图片已保存", Toast.LENGTH_SHORT).show());
    }
    public void downloadImageFromUrl(Context context, String url) {
        new Thread(() -> {
            try {
                var imageUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException("服务器响应码: " + connection.getResponseCode());
                }

                // 推测文件类型和扩展名
                String contentType = connection.getContentType(); // 如 image/gif, image/png
                String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType);
                if (extension == null) extension = "jpg";

                String fileName = "IMG_" + System.currentTimeMillis() + "." + extension;

                InputStream input = connection.getInputStream();
                saveStreamToGallery(context, input, fileName, contentType);

                input.close();
                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "下载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void finish() {
        super.finish();
        // 离开动画
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    public static void start(Context context, String imageUrl) {
        Intent intent = new Intent(context, ImageViewActivity.class);
        var data = new Bundle();
        data.putString(EXTRA_IMAGE_URL,imageUrl);
        intent.putExtras(data);
        context.startActivity(intent);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 默认返回栈顶页面
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}