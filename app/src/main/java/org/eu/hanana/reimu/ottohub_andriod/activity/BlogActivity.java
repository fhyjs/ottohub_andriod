package org.eu.hanana.reimu.ottohub_andriod.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.eu.hanana.reimu.ottohub_andriod.activity.FragActivity.ARG_DATA;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditAdapter.ARG_RESULT;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditAdapter.ARG_TARGET;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment.TYPE_BLOG;
import static org.eu.hanana.reimu.ottohub_andriod.ui.message.MessageListFragment.ARG_TYPE;
import static org.eu.hanana.reimu.ottohub_andriod.util.UiUtil.shareText;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
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
import org.eu.hanana.reimu.lib.ottohub.api.common.EmptyResult;
import org.eu.hanana.reimu.lib.ottohub.api.engagement.EngagementResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.MyAppApplicationLike;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.service.CopyService;
import org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditAdapter;
import org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ClipboardUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.CustomWebView;
import org.eu.hanana.reimu.ottohub_andriod.util.SharedPreferencesKeys;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

public class BlogActivity extends BaseActivity {
    public static final String KEY_BID="bid";
    public static final String KEY_TYPE= ARG_TYPE;
    public static final String KEY_DATA= ARG_DATA;
    public static final String TYPE_VIEW="v";
    public static final String TYPE_AUDIT="a";
    public static final String TYPE_PREVIEW="p";
    public int bid;
    private CustomWebView webView;
    protected String blogPage = CustomWebView.internal+"web/blog/index.html";
    protected BlogResult blogResult;
    protected boolean inited=false;
    private View currentPage;
    private ViewGroup page1, page2;
    public String type = TYPE_VIEW;
    @Nullable
    public String data = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog);

        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (getIntent().getExtras().containsKey(KEY_DATA)){
            data=getIntent().getExtras().getString(KEY_DATA);
        }
        if (getIntent().getExtras().containsKey(KEY_TYPE)){
            type=getIntent().getExtras().getString(KEY_TYPE);
        }
        setTitle("loading...");

        bid=getIntent().getExtras().getInt(KEY_BID);
        webView = findViewById(R.id.wvContent);
        webView.allowJsHeightAuto=false;
        webView.addJavascriptInterface(new JsBridge(this), "blog"); // "AndroidBridge" 是 JS 调用的对象名
        webView.loadUrl(blogPage);

        page1 = findViewById(R.id.scrollView);
        page2 = findViewById(R.id.fragment_container);

    }
    public void init(){
        inited=true;
        Thread thread = new Thread(() -> {
            if (TYPE_VIEW.equals(type)) {
                blogResult = MyAppApplicationLike.getInstance().getOttohubApi().getBlogApi().get_blog_detail(bid);
            }else if (TYPE_AUDIT.equals(type)|TYPE_PREVIEW.equals(type)) {
                blogResult = new Gson().fromJson(data,BlogResult.class);
            }
            runOnUiThread(this::initUI);
        });
        thread.setUncaughtExceptionHandler((t, e) -> runOnUiThread(()-> AlertUtil.showError(BlogActivity.this,"ERROR: "+e)));
        thread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();

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
        if (isDestroyed()||isFinishing()) return;
        Glide.with(this)
                .load(blogResult.avatar_url)
                .circleCrop()
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into((ImageView) findViewById(R.id.ivAvatar));
        findViewById(R.id.clAuthorInfo).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(ProfileActivity.KEY_UID,blogResult.uid);
            intent.putExtras(bundle);
            startActivity(intent);
        });
        findViewById(R.id.btn_share).setOnClickListener(v -> {
            var txt = v.getContext().getString(R.string.share_content,blogResult.title,"https://m.ottohub.cn/b/"+bid);
            ClipboardUtil.copyToClipboard(v.getContext(),txt);
            shareText(v.getContext(),txt);
        });
        findViewById(R.id.btn_like).setOnClickListener(v -> {
            if (MyAppApplicationLike.getInstance().getOttohubApi().getLoginToken()==null) {
                AlertUtil.showError(this,getString(R.string.not_login)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                EngagementResult engagementResult = MyAppApplicationLike.getInstance().getOttohubApi().getEngagementApi().like_blog(bid);
                ApiUtil.throwApiError(engagementResult);
                blogResult.like_count=engagementResult.like_count;
                blogResult.if_like=engagementResult.if_like;
                runOnUiThread(this::updateActionBtns);
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(BlogActivity.this));
            thread.start();
        });
        findViewById(R.id.btn_favourite).setOnClickListener(v -> {
            if (MyAppApplicationLike.getInstance().getOttohubApi().getLoginToken()==null) {
                AlertUtil.showError(this,getString(R.string.not_login)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                EngagementResult engagementResult = MyAppApplicationLike.getInstance().getOttohubApi().getEngagementApi().favorite_blog(bid);
                ApiUtil.throwApiError(engagementResult);
                blogResult.favorite_count=engagementResult.favorite_count;
                blogResult.if_favorite=engagementResult.if_favorite;
                runOnUiThread(this::updateActionBtns);
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(BlogActivity.this));
            thread.start();
        });
        findViewById(R.id.btn_report).setOnClickListener(v -> {
            if (MyAppApplicationLike.getInstance().getOttohubApi().getLoginToken()==null) {
                AlertUtil.showError(this,getString(R.string.not_login)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                EmptyResult emptyResult = MyAppApplicationLike.getInstance().getOttohubApi().getModerationApi().report_blog(bid);
                ApiUtil.throwApiError(emptyResult);
                runOnUiThread(()->{
                    AlertUtil.showMsg(this, getString(R.string.report), getString(R.string.ok)).show();
                });
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(BlogActivity.this));
            AlertUtil.showYesNo(this, getString(R.string.report), getString(R.string.issure), (dialog, which) -> thread.start(),null).show();
        });
        updateActionBtns();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, CommentFragmentBase.newInstance(bid,0,CommentFragmentBase.TYPE_BLOG))
                .commit();
        addMenuProvider(new MyMenuProvider(),this);
        currentPage = page1; // 初始为第一页
        TabLayout tabLayout = findViewById(R.id.tlPage);
        var tab_comment = tabLayout.newTab();
        var tab_blog = tabLayout.newTab();
        tabLayout.addTab(tab_blog);
        tab_blog.setText(R.string.blogs);
        tabLayout.addTab(tab_comment);
        tab_comment.setText(R.string.comment);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab==tab_blog){
                    switchPage(page1,false);
                }else {
                    switchPage(page2,true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentPage==page2) {
                    tabLayout.selectTab(tab_blog);
                } else {
                    setEnabled(false); // 暂时释放拦截
                    getOnBackPressedDispatcher().onBackPressed(); // 调用系统默认行为
                    setEnabled(true);
                }
            }
        });
        findViewById(R.id.btn_approve).setOnClickListener(v -> {
            AlertUtil.showYesNo(this,getString(R.string.approve),getString(R.string.issure),(dialog, which) -> {
                Intent intent = new Intent();
                intent.putExtra(ARG_RESULT, true);
                intent.putExtra(AuditAdapter.ARG_TYPE, TYPE_BLOG);
                intent.putExtra(ARG_TARGET, bid);
                setResult(RESULT_OK, intent);
                finish();
            },null).show();
        });
        findViewById(R.id.btn_reject).setOnClickListener(v -> {
            AlertUtil.showYesNo(this,getString(R.string.reject),getString(R.string.issure),(dialog, which) -> {
                Intent intent = new Intent();
                intent.putExtra(ARG_RESULT, false);
                intent.putExtra(AuditAdapter.ARG_TYPE, TYPE_BLOG);
                intent.putExtra(ARG_TARGET, bid);
                setResult(RESULT_OK, intent);
                finish();
            },null).show();
        });
        if (TYPE_AUDIT.equals(type)) {
            tabLayout.setVisibility(GONE);
            setTitle(String.format(Locale.getDefault(),"%s - %s",getString(R.string.audit_title),getTitle()));
            findViewById(R.id.clAuthorInfo).setVisibility(GONE);

            findViewById(R.id.group_user).setVisibility(GONE);
            findViewById(R.id.group_audit).setVisibility(VISIBLE);
        }else if (TYPE_PREVIEW.equals(type)) {
            tabLayout.setVisibility(GONE);
            findViewById(R.id.ll_actionBar).setVisibility(GONE);
        }
        findViewById(R.id.btn_download).setOnClickListener(v -> {
            Intent intent = new Intent(this, CopyService.class);
            intent.putExtra("uri", Uri.parse("content://org.eu.hanana.reimu.ottohub_andriod.provider.download/blog?bid="+bid));
            intent.putExtra("fileName", "blog_"+bid+".zip");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ 必须用 startForegroundService
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            Toast.makeText(this,R.string.notise_msg,Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
    private class MyMenuProvider implements MenuProvider {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            // 加载菜单布局
            Drawable drawable = AppCompatResources.getDrawable(BlogActivity.this, R.drawable.arrow_downward_24dp);
            drawable.setTintList(ContextCompat.getColorStateList(BlogActivity.this,R.color.white));
            menu.add(Menu.NONE,1,Menu.NONE,getString(R.string.toBottom)).setIcon(drawable).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        @Override
        public void onPrepareMenu(@NonNull Menu menu) {
            // 动态调整菜单项（替代旧的 onPrepareOptionsMenu）
            MenuItem item = menu.findItem(1);
            if (item != null) {
                item.setVisible(true);
                item.setEnabled(true);
            }
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            // 处理点击事件
            int id = menuItem.getItemId();
            if (id == 1) {
                findViewById(R.id.scrollView).scrollTo(0,findViewById(R.id.ll_actionBar).getTop());
                return true;
            }
            return false;
        }
    }
    private void updateActionBtns() {
        ((TextView) findViewById(R.id.btn_like)).setText(String.format(Locale.getDefault(),"%d%s",blogResult.like_count,getString(R.string.like)));
        ((TextView) findViewById(R.id.btn_favourite)).setText(String.format(Locale.getDefault(),"%d%s",blogResult.favorite_count,getString(R.string.favourite)));
        if (blogResult.if_like==1){
            ((MaterialButton) findViewById(R.id.btn_like)).setIcon(AppCompatResources.getDrawable(this,R.drawable.thumb_up_24dp_fill));
        }else {
            ((MaterialButton) findViewById(R.id.btn_like)).setIcon(AppCompatResources.getDrawable(this,R.drawable.thumb_up_24dp));
        }
        if (blogResult.if_favorite==1){
            ((MaterialButton) findViewById(R.id.btn_favourite)).setIcon(AppCompatResources.getDrawable(this,R.drawable.kitchen_24dp_fill));
        }else {
            ((MaterialButton) findViewById(R.id.btn_favourite)).setIcon(AppCompatResources.getDrawable(this,R.drawable.kitchen_24dp));
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
        public boolean isJavaMd(){
            return PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance().getApplicationContext()).getBoolean("java_md",false);
        }
        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
        @JavascriptInterface
        public int getBgColor() {
            return ThemeUtil.getTheme(BlogActivity.this).getColorBackground();
        }
        @JavascriptInterface
        public int getTextColor() {
            return ThemeUtil.getTheme(BlogActivity.this).getColorOnPrimary();
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
    private void switchPage(View nextPage, boolean toLeft) {
        if (currentPage == nextPage) return;

        int width = currentPage.getWidth();
        int fromToX = toLeft ? -width : width;
        int toFromX = toLeft ? width : -width;

        // 设置目标页起始位置
        nextPage.setTranslationX(toFromX);
        nextPage.setVisibility(VISIBLE);
        nextPage.postInvalidate();
        // 动画：当前页滑出，目标页滑入
        ViewPropertyAnimator hideAnim = currentPage.animate().translationX(fromToX).setDuration(300);
        ViewPropertyAnimator showAnim = nextPage.animate().translationX(0).setDuration(300);

        // 动画结束后隐藏当前页
        hideAnim.withEndAction(() -> {
            currentPage.setVisibility(GONE);
            currentPage = nextPage;
        });
    }
}