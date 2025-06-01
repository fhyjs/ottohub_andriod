package org.eu.hanana.reimu.ottohub_andriod.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CustomWebView extends WebView {
    private ProgressBar progressBar;
    public static final String internal = "https://android_asset/";
    public WebSettings settings;

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // 添加进度条
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 10, 0, 0));
        addView(progressBar);
        
        // 基础设置
        settings = super.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setSupportMultipleWindows(true);
        settings.setSupportZoom(true);
        setWebContentsDebuggingEnabled(true);
        // 客户端设置
        setWebChromeClient(new CustomWebChromeClient());
        setWebViewClient(new CustomWebViewClient());
    }

    @NonNull
    @Override
    public WebSettings getSettings() {
        return settings;
    }

    public class CustomWebChromeClient extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView.HitTestResult result = view.getHitTestResult();

            String url = null;

            if (result != null && result.getExtra() != null) {
                // 某些情况下可以直接获取 URL
                url = result.getExtra();
            } else {
                // 更常见的方式：使用新建的 WebView 获取 URL
                WebView newWebView = new WebView(view.getContext());
                newWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                        // 一旦开始加载，立即用默认浏览器打开
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        view.getContext().startActivity(intent);
                    }
                });

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();

                return true; // 表示拦截了创建窗口，交由自定义处理
            }

            if (url != null) {
                // 打开外部浏览器
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(intent);
            }

            return false; // 不创建 WebView 的新窗口
        }
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    }

    public class CustomWebViewClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (!request.getUrl().toString().startsWith(internal))
                return super.shouldInterceptRequest(view, request);
            // 拦截请求并返回本地资源
            String url = request.getUrl().toString();
            String contentType = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    contentType = Files.probeContentType(Paths.get(url));
                }
            } catch (IOException ignored) {
            }
            // 处理文件
            return getWebResourceResponseFromAssets(url, contentType);
        }

        private WebResourceResponse getWebResourceResponseFromAssets(String url, String mimeType) {
            try {
                // 从 URL 提取资源路径
                String assetPath = url.replace(internal, "");

                // 打开资源流
                InputStream inputStream = getContext().getAssets().open(assetPath);

                // 返回 WebResourceResponse
                WebResourceResponse webResourceResponse = new WebResourceResponse(mimeType, "UTF-8", inputStream);
                //inputStream.close();
                return webResourceResponse;

            } catch (IOException e) {
                // 资源未找到
                return new WebResourceResponse(mimeType, "UTF-8", new ByteArrayInputStream(e.toString().getBytes(StandardCharsets.UTF_8)));
            }
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            // 处理本地链接
            String url = request.getUrl().toString();
            if (url.startsWith(internal)) {
                return true; // WebView 处理
            }
            return super.shouldOverrideUrlLoading(view, request);
        }
    }
}