package org.eu.hanana.reimu.ottohub_andriod.activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

import org.eu.hanana.reimu.lib.ottohub.api.blog.BlogResult;
import org.eu.hanana.reimu.ottohub_andriod.MainActivity;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.CustomWebView;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

public class BlogActivity extends AppCompatActivity {
    public static final String KEY_BID="bid";
    public int bid;
    private WebView webView;
    protected String blogPage = CustomWebView.internal+"web/blog/index.html";
    protected BlogResult blogResult;
    protected boolean inited=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("loading...");

        bid=getIntent().getExtras().getInt(KEY_BID);
        webView = findViewById(R.id.wvContent);
        webView.addJavascriptInterface(new JsBridge(this), "blog"); // "AndroidBridge" 是 JS 调用的对象名
        webView.loadUrl(blogPage);
    }
    public void init(){
        inited=true;
        Thread thread = new Thread(() -> {
            blogResult=MyApp.getInstance().getOttohubApi().getBlogApi().get_blog_detail(bid);
            runOnUiThread(this::initUI);
        });
        thread.setUncaughtExceptionHandler((t, e) -> runOnUiThread(()-> AlertUtil.showError(BlogActivity.this,"ERROR: "+e)));
        thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    public void initUI(){
        setTitle(blogResult.title);
        ((TextView) findViewById(R.id.tvAuthor)).setText(blogResult.username);
        Glide.with(this)
                .load(blogResult.avatar_url)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into((ImageView) findViewById(R.id.ivAvatar));
        ((TextView) findViewById(R.id.btn_like)).setText(String.format(Locale.getDefault(),"%d%s",blogResult.like_count,getString(R.string.like)));
        ((TextView) findViewById(R.id.btn_favourite)).setText(String.format(Locale.getDefault(),"%d%s",blogResult.favorite_count,getString(R.string.favourite)));
        findViewById(R.id.clAuthorInfo).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(ProfileActivity.KEY_UID,blogResult.uid);
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 默认返回栈顶页面
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            if (blogResult==null){
                if (!inited)
                    init();
                return "loading";
            }
            return new Gson().toJson(blogResult);
        }
        @JavascriptInterface
        public String markdown(String md){
            Node document = parser.parse(md);
            // 输出 HTML
            return renderer.render(document);
        }
    }
}