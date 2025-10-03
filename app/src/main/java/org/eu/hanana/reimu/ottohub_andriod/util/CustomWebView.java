package org.eu.hanana.reimu.ottohub_andriod.util;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import lombok.Setter;

public class CustomWebView extends WebView {
    private ProgressBar progressBar;
    public static final String internal = "https://android_asset/";
    public WebSettings settings;
    private int lastHeight = 0;
    @Setter
    private ValueCallback<Uri[]> filePathCallback;
    private FileChooserListener fileChooserListener;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomWebView(Context context) {
        this(context,null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public int getContentHeight() {
        return super.getContentHeight();
    }
    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    private void init(Context context) {
        // 添加进度条
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 10, 0, 0));
        addView(progressBar);

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        // 基础设置
        settings = super.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
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
        addJavascriptInterface(new Object(){
            @JavascriptInterface
            public String intArgbToRgba(int argbInt) {
                // >>> 是无符号右移
                int a = (argbInt >>> 24) & 0xFF;  // 0-255
                int r = (argbInt >> 16) & 0xFF;
                int g = (argbInt >> 8) & 0xFF;
                int b = argbInt & 0xFF;

                // 转成 0~1 的浮点 alpha
                float alpha = a / 255f;

                return String.format(Locale.getDefault(),"rgba(%d, %d, %d, %.2f)", r, g, b, alpha);
            }

            @JavascriptInterface
            public void setHeight(int height){
                post(()->{
                    var hv = height;
                    hv=(int) (hv * getScale());
                    if (hv == lastHeight) return; // 防止重复设置
                    lastHeight = hv;

                    ViewGroup.LayoutParams layoutParams = getLayoutParams();
                    layoutParams.height=hv;
                    measure(getWidth(),hv);
                    setLayoutParams(layoutParams);
                    requestLayout();
                });
            }
            @JavascriptInterface
            public void showToast(String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
            @JavascriptInterface
            public int getBgColor() {
                return ThemeUtil.getTheme(getContext()).getColorBackground();
            }
            @JavascriptInterface
            public int getTextColor() {
                return ThemeUtil.getTheme(getContext()).getColorOnPrimary();
            }
            @JavascriptInterface
            public String getToken() {
                return ApiUtil.getAppApi().getLoginToken();
            }
            @JavascriptInterface
            public String getUid() {
                SharedPreferences sharedPreferences = MyApp.getInstance().getSharedPreferences(SharedPreferencesKeys.Perf_Auth, MODE_PRIVATE);
                return sharedPreferences.getString(SharedPreferencesKeys.Key_Username,"");
            }
            @JavascriptInterface
            public String getPassWd() {
                SharedPreferences sharedPreferences = MyApp.getInstance().getSharedPreferences(SharedPreferencesKeys.Perf_Auth, MODE_PRIVATE);
                return sharedPreferences.getString(SharedPreferencesKeys.Key_Passwd,"");
            }
        },"hanana");
    }

    @NonNull
    @Override
    public WebSettings getSettings() {
        return settings;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void openUrl(String url){
        UiUtil.openUrl(getContext(),url);
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
                        openUrl(url);
                    }
                });

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();

                return true; // 表示拦截了创建窗口，交由自定义处理
            }

            if (url != null) {
                if (url.startsWith(internal)) return false;
                // 打开外部浏览器
                openUrl(url);
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
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            new MaterialAlertDialogBuilder(view.getContext())
                    .setTitle(R.string.tip)
                    .setMessage(message)
                    .setPositiveButton(R.string.conform, (dialog, which) -> result.confirm())
                    .setOnCancelListener(dialog -> result.cancel()) // 弹窗关闭时取消
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            new MaterialAlertDialogBuilder(view.getContext())
                    .setTitle(R.string.conform)
                    .setMessage(message)
                    .setPositiveButton(R.string.conform, (dialog, which) -> result.confirm())
                    .setNegativeButton(R.string.cancel, (dialog, which) -> result.cancel())
                    .setOnCancelListener(dialog -> result.cancel()) // 弹窗关闭时取消
                    .show();
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            final EditText input = new EditText(view.getContext());
            input.setText(defaultValue);

            new MaterialAlertDialogBuilder(view.getContext())
                    .setTitle(message)
                    .setView(input)
                    .setPositiveButton(R.string.conform, (dialog, which) -> result.confirm(input.getText().toString()))
                    .setNegativeButton(R.string.cancel, (dialog, which) -> result.cancel())
                    .setOnCancelListener(dialog -> result.cancel()) // 弹窗关闭时取消
                    .show();
            return true;
        }
        @Override
        public boolean onShowFileChooser(WebView webView,
                                         ValueCallback<Uri[]> filePathCallback,
                                         FileChooserParams fileChooserParams) {
            if (CustomWebView.this.filePathCallback != null) {
                CustomWebView.this.filePathCallback.onReceiveValue(null);
            }
            CustomWebView.this.filePathCallback = filePathCallback;

            if (fileChooserListener != null) {
                fileChooserListener.onShowFileChooser(fileChooserParams.createIntent());
            }
            return true;
        }
    }

    public void onFileChooserResult(int resultCode, Intent data) {
        if (filePathCallback != null) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    // 回调接口
    public interface FileChooserListener {
        void onShowFileChooser(Intent intent);
    }
    public class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {
            super.onPageStarted(webView, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String js = "var tag = document.createElement('script');" +
                    "tag.type = 'text/javascript';" +
                    "tag.src = \""+internal+"web/assets/webview.js\";" +
                    "document.head.appendChild(tag);";
            view.evaluateJavascript(js, null);
        }

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
                String assetPath = url.replace(internal, "").split("\\?")[0];

                // 打开资源流
                InputStream inputStream = getContext().getAssets().open(assetPath);

                // 返回 WebResourceResponse
                WebResourceResponse webResourceResponse = new WebResourceResponse(mimeType, "UTF-8", inputStream);
                //inputStream.close();
                return webResourceResponse;

            } catch (IOException e) {
                String mimeTyped = "text/plain"; // 或 "text/html" 如果你要返回 HTML
                String encoding = "UTF-8";
                String data = "资源未找到: " + e;
                // API 21+ 可以设置状态码和原因
                return new WebResourceResponse(
                        mimeTyped,
                        encoding,
                        404,            // HTTP 状态码
                        "Not Found",    // reasonPhrase
                        null,           // headers
                        new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))
                );
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