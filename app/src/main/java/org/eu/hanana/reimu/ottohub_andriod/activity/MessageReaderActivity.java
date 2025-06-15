package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.gitlab.GitLabExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import org.eu.hanana.reimu.lib.ottohub.api.im.MessageResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.CustomWebView;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

public class MessageReaderActivity extends AppCompatActivity {
    protected String webPage = CustomWebView.internal+"web/message/index.html";
    private WebView webView;
    protected boolean finish, inited=false;
    public MessageResult messageData;
    public static final String ARG_MID = "mid";
    public int mid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_message_reader);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (getIntent().getExtras()!=null)
            mid=getIntent().getExtras().getInt(ARG_MID);
        setTitle(getString(R.string.loading));
        webView = findViewById(R.id.wvContent);
        webView.addJavascriptInterface(new MessageReaderActivity.JsBridge(this), "blog"); // "AndroidBridge" 是 JS 调用的对象名
        webView.loadUrl(webPage);
    }
    protected void init(){
        inited=true;
        Thread thread = new Thread(() -> {
            MessageResult messageResult = ApiUtil.getAppApi().getMessageApi().read_message(mid);
            ApiUtil.throwApiError(messageResult);
            this.messageData = messageResult;
            runOnUiThread(this::initUI);
        });
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this));
        thread.start();
    }

    private void initUI() {
        setTitle(getString(R.string.message_title,messageData.sender_name,messageData.receiver_name));
        finish=true;
    }

    protected class JsBridge {
        private final @NotNull Parser parser;
        private final @NotNull HtmlRenderer renderer;
        private Context context;

        public JsBridge(Context context) {
            this.context = context;
            MutableDataSet options = new MutableDataSet();

            // 注册所有常见扩展
            options.set(Parser.EXTENSIONS, Arrays.asList(
                    TablesExtension.create(),
                    StrikethroughExtension.create(),
                    TaskListExtension.create(),
                    FootnoteExtension.create(),
                    AutolinkExtension.create(),
                    TypographicExtension.create(),
                    AnchorLinkExtension.create(),
                    TocExtension.create(),
                    AbbreviationExtension.create(),
                    WikiLinkExtension.create(),
                    AttributesExtension.create(),
                    GitLabExtension.create()
            ));
            parser = Parser.builder(options)
                    .build();

            renderer = HtmlRenderer.builder(options)
                    .build();
        }

        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public String getData() {
            if (!finish){
                if (!inited)
                    init();
                return "loading";
            }
            return new Gson().toJson(messageData);
        }
        @JavascriptInterface
        public String markdown(String md){
            Node document = parser.parse(md);
            // 输出 HTML
            return renderer.render(document);
        }
        @JavascriptInterface
        public String getTitle(){
            return getString(R.string.message_title,String.format(Locale.getDefault(),"%s(uid%d)",messageData.sender_name,messageData.sender),String.format(Locale.getDefault(),"%s(uid%d)",messageData.receiver_name,messageData.receiver));
        }
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