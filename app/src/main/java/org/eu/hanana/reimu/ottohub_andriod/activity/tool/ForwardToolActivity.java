package org.eu.hanana.reimu.ottohub_andriod.activity.tool;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.BaseActivity;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.BiliPlaybackData;
import org.eu.hanana.reimu.ottohub_andriod.util.BiliPlaybackUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.CustomWebView;
import org.eu.hanana.reimu.ottohub_andriod.util.ft.DownloadVideoHandler;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import kotlin.Pair;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ForwardToolActivity extends BaseActivity {
    private CustomWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forward_tool);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.forward_tool);
        webView=findViewById(R.id.wv);
        webView.loadUrl(CustomWebView.internal+"web/ft/index.html");
        webView.addJavascriptInterface(new FtJsInterface(),"ft");
        webView.allowJsHeightAuto=false;
        // 注册返回键回调
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack(); // WebView 后退
                } else {
                    // 禁用回调后再调用默认返回行为
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    private class FtJsInterface {
        @JavascriptInterface
        public String getVidInfo(String type,String vid){
            type = type.equals("av")?"aid":"bvid";
            try {
                OkHttpClient httpClient = new OkHttpClient();
                HttpUrl url = HttpUrl.parse("https://api.bilibili.com/x/web-interface/view")
                        .newBuilder()
                        .addQueryParameter(type, vid)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .header("Referer", "https://bilibili.com/")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36")
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    String responseBody = response.body().string();
                    JsonElement jsonElement = JsonParser.parseString(responseBody);

                    if (jsonElement.getAsJsonObject().has("data")) {
                        JsonObject data = jsonElement.getAsJsonObject().getAsJsonObject("data");
                        if (data.has("pic")) {
                            String picUrl = data.get("pic").getAsString();
                            data.addProperty("pic", ApiUtil.downloadFileToBase64(picUrl));
                        }
                    }
                    if (jsonElement.getAsJsonObject().get("code").getAsInt()!=0){
                        runOnUiThread(()->AlertUtil.showError(ForwardToolActivity.this,jsonElement.getAsJsonObject().get("message").getAsString()).show());
                    }
                    return jsonElement.toString();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        private final OkHttpClient client = new OkHttpClient();

        @JavascriptInterface
        public String getTagInfo(String aid) {
            String url = "https://api.bilibili.com/x/tag/archive/tags?aid=" + aid;

            Request request = new Request.Builder()
                    .url(url)
                    //.header("Referer", "https://bilibili.com/") // 可选
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36")
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("HTTP " + response.code());
                }
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        }
        @JavascriptInterface
        public String getUrlInfo(String type,String cid,String vid){
            try {
                // 获取请求参数
                type = type.equals("av") ? "avid" : "bvid";
                String sessData = "";
                //if (webUi.getSessionManage().getUser(httpServerRequest).data.has("bilibili_sess")) {
                //    sessData = webUi.getSessionManage().getUser(httpServerRequest).data.get("bilibili_sess").getAsString();
                //}
                boolean login = !sessData.isEmpty();
                Pair<String, List<BiliPlaybackData>> data;

                // 根据是否登录选择 Dash 或 Mp4
                if (login) {
                    data = BiliPlaybackUtil.getDashPlaybackData(sessData, type, vid, cid);
                } else {
                    data = BiliPlaybackUtil.getMp4PlaybackData(type, vid, cid);
                }

                // 返回 JSON 字符串
                return data.getFirst();

            } catch (Exception e) {
                e.printStackTrace();
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }

        }
        @JavascriptInterface
        public void start(String json){
            runOnUiThread(()->exec(json));
        }
    }

    private void exec(String json) {
        DownloadVideoHandler.Runner runner = new DownloadVideoHandler.Runner(new DownloadVideoHandler.OutCtrl() {
            @Override
            public void sendString(String string) {
                Log.d("FTA",string);
                runOnUiThread(()->webView.evaluateJavascript("onmessage('"+string+"')",null));
            }

            @Override
            public void sendClose() {
                runOnUiThread(()->webView.evaluateJavascript("onclose('"+111+"')",null));
            }
        });
        Thread thread = new Thread(() -> {
            runner.input(JsonParser.parseString(json));
        });
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this));
        thread.start();
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