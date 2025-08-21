package org.eu.hanana.reimu.ottohub_andriod.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.ApiBase;
import org.eu.hanana.reimu.lib.ottohub.api.blog.BlogResult;
import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OttohubContentProvider extends ContentProvider {
    public static final String AUTHORITY = "org.eu.hanana.reimu.ottohub_andriod.provider.download";
    @Override
    public boolean onCreate() {
        return true;
    }
    private Map<String,Long> urlSizes = new HashMap<>();

    // 通过 openFile 提供文件访问
    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new FileNotFoundException("Read only content");
        }
        var path =uri.getPath();
        if (path.equals("/url")){
            try {
                // 获取 URL
                String urlStr1 = uri.getQueryParameter("url");
                HttpURLConnection conn1 = (HttpURLConnection) new URL(urlStr1).openConnection();
                conn1.setRequestMethod("HEAD"); // 只获取头部
                conn1.connect();
                long contentLength = -1; // 可能为 -1
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentLength = conn1.getContentLengthLong();
                }
                conn1.disconnect();

                // 保存 contentLength，在 query 中返回
                urlSizes.put(uri.toString(), contentLength);
                // 创建管道
                ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
                ParcelFileDescriptor readSide = pipe[0];
                ParcelFileDescriptor writeSide = pipe[1];

                // 异步下载 HTTP 文件并写入管道
                new Thread(() -> {
                    try (OutputStream os = new ParcelFileDescriptor.AutoCloseOutputStream(writeSide)) {
                        var urlStr = uri.getQueryParameter("url");
                        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                        conn.connect();

                        try (InputStream is = conn.getInputStream()) {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = is.read(buffer)) != -1) {
                                os.write(buffer, 0, len);
                            }
                        }

                        os.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 可以写入错误提示到管道或关闭管道
                    }
                }).start();

                return readSide;

            } catch (Exception e) {
                throw new FileNotFoundException("无法创建虚拟文件: " + e.getMessage());
            }
        } else if (path.equals("/blog")) {
            return blog(Integer.parseInt(Objects.requireNonNull(uri.getQueryParameter("bid"))));
        } else if (path.equals("/video")) {
            return video(Integer.parseInt(Objects.requireNonNull(uri.getQueryParameter("vid"))));
        }
        throw new FileNotFoundException("Unsupported URI: " + uri);
    }
    private ParcelFileDescriptor video(int vid){
        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            ParcelFileDescriptor readSide = pipe[0];
            ParcelFileDescriptor writeSide = pipe[1];

            new Thread(() -> {
                try (OutputStream os = new ParcelFileDescriptor.AutoCloseOutputStream(writeSide);
                     ZipOutputStream zos = new ZipOutputStream(os)) {

                    // 动态生成 ZIP 条目
                    String[] files = {"video.json", "cover.jpg","user_avatar.jpg","video.mp4"}; // 示例，可以从 URI query 获取实际文件
                    zos.putNextEntry(new ZipEntry("video.json"));
                    VideoResult videoResult = ApiUtil.getAppApi().getVideoApi().get_video_detail(vid);
                    String json = new Gson().toJson(videoResult);
                    zos.write(json.getBytes(StandardCharsets.UTF_8));
                    zos.putNextEntry(new ZipEntry("user_avatar.jpg"));
                    zos.write(ApiUtil.downloadFile(videoResult.avatar_url));
                    zos.putNextEntry(new ZipEntry("cover.jpg"));
                    zos.write(ApiUtil.downloadFile(videoResult.cover_url));
                    zos.putNextEntry(new ZipEntry("video.mp4"));
                    zos.write(ApiUtil.downloadFile(videoResult.video_url));

                    zos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            return readSide;
        } catch (Exception e) {
            Log.e("ZipContentProvider", "创建虚拟 ZIP 失败", e);
            return null;
        }
    }
    private ParcelFileDescriptor blog(int bid){
        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            ParcelFileDescriptor readSide = pipe[0];
            ParcelFileDescriptor writeSide = pipe[1];

            new Thread(() -> {
                try (OutputStream os = new ParcelFileDescriptor.AutoCloseOutputStream(writeSide);
                     ZipOutputStream zos = new ZipOutputStream(os)) {

                    // 动态生成 ZIP 条目
                    String[] files = {"blog.json", ".jpg"}; // 示例，可以从 URI query 获取实际文件
                    zos.putNextEntry(new ZipEntry("blog.json"));
                    BlogResult blogDetail = ApiUtil.getAppApi().getBlogApi().get_blog_detail(bid);
                    String json = new Gson().toJson(blogDetail);
                    zos.write(json.getBytes(StandardCharsets.UTF_8));
                    zos.putNextEntry(new ZipEntry("user_avatar.jpg"));
                    zos.write(ApiUtil.downloadFile(blogDetail.avatar_url));

                    zos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            return readSide;
        } catch (Exception e) {
            Log.e("ZipContentProvider", "创建虚拟 ZIP 失败", e);
            return null;
        }
    }
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
        });

        String name = uri.getLastPathSegment();
        long size = -1;
        if (urlSizes.containsKey(uri.toString())) {
            size = urlSizes.get(uri.toString());
        }

        cursor.addRow(new Object[]{name, size});
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "application/octet-stream";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }
}