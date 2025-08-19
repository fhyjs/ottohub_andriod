package org.eu.hanana.reimu.ottohub_andriod.activity;

import static org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity.TYPE_AUDIT;
import static org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity.TYPE_PREVIEW;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.blog.BlogResult;
import org.eu.hanana.reimu.lib.ottohub.api.common.EmptyResult;
import org.eu.hanana.reimu.lib.ottohub.api.creator.LoadBlogResult;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class UploadBlogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_blog);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.upload_blog);
        findViewById(R.id.btn_preview).setOnClickListener(v -> {
            var br = new BlogResult();
            br.title="["+getString(R.string.preview)+"] "+((EditText) findViewById(R.id.et_title)).getText().toString();
            br.content=((EditText) findViewById(R.id.et_content)).getText().toString();
            LocalDateTime now = LocalDateTime.now();
            br.time = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            br.username = getString(R.string.preview);
            br.uid = Integer.parseInt(ApiUtil.getAppApi().getLoginResult().uid);
            br.avatar_url = ApiUtil.getAppApi().getLoginResult().avatar_url;

            Intent intent = new Intent(this, BlogActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(BlogActivity.KEY_BID,0);
            bundle.putString(BlogActivity.KEY_DATA,new Gson().toJson(br));
            bundle.putString(BlogActivity.KEY_TYPE,TYPE_PREVIEW);
            intent.putExtras(bundle);
            startActivity(intent);
        });
        findViewById(R.id.btn_save_draft).setOnClickListener(v -> {
            AlertDialog alertDialog = AlertUtil.showLoading(this, getString(R.string.loading));
            alertDialog.show();
            Thread thread = new Thread(() -> {
                EmptyResult loadBlogResult = ApiUtil.getAppApi().getCreatorApi().save_blog(((EditText) findViewById(R.id.et_content)).getText().toString());
                ApiUtil.throwApiError(loadBlogResult);
                runOnUiThread(alertDialog::dismiss);
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this){
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    super.uncaughtException(thread, ex);
                    alertDialog.dismiss();
                }
            });
            thread.start();
        });
        AlertDialog alertDialog = AlertUtil.showLoading(this, getString(R.string.loading));
        alertDialog.show();
        Thread thread = new Thread(() -> {
            LoadBlogResult loadBlogResult = ApiUtil.getAppApi().getCreatorApi().load_blog();
            ApiUtil.throwApiError(loadBlogResult);
            runOnUiThread(()->{
                alertDialog.dismiss();
                ((EditText) findViewById(R.id.et_content)).setText(loadBlogResult.content);
            });
        });
        thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this){
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                super.uncaughtException(thread, ex);
                alertDialog.dismiss();
            }
        });
        thread.start();
        findViewById(R.id.btn_add).setOnClickListener(v -> {
            UiUtil.insertTextAtCursor(findViewById(R.id.et_content),String.format(Locale.getDefault(),"\n![](%s)\n",((EditText) findViewById(R.id.et_imgurl)).getText().toString()));
            ((EditText) findViewById(R.id.et_imgurl)).getText().clear();
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
}