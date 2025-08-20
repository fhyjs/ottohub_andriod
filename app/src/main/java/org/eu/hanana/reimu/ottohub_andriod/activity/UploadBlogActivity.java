package org.eu.hanana.reimu.ottohub_andriod.activity;

import static org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity.TYPE_PREVIEW;
import static org.eu.hanana.reimu.ottohub_andriod.util.FileUtil.getFileSize;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eu.hanana.reimu.lib.ottohub.api.ApiBase;
import org.eu.hanana.reimu.lib.ottohub.api.blog.BlogResult;
import org.eu.hanana.reimu.lib.ottohub.api.common.EmptyResult;
import org.eu.hanana.reimu.lib.ottohub.api.creator.LoadBlogResult;
import org.eu.hanana.reimu.lib.ottohub.api.creator.SubmitBlogResult;
import org.eu.hanana.reimu.lib.ottohub.util.InputStreamRequestBody;
import org.eu.hanana.reimu.lib.ottohub.util.ProgressedRequestBody;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;

public class UploadBlogActivity extends AppCompatActivity {
    // 先定义一个 ActivityResultLauncher
    private ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    AlertDialog alertDialog = AlertUtil.showLoading(this, getString(R.string.loading));
                    alertDialog.show();
                    // 这里就是用户选中的文件Uri
                    Log.d("File", "选中的文件: " + uri);
                    Thread thread = new Thread(() -> {
                        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                            ApiBase apiBase = (ApiBase) ApiUtil.getAppApi().getBlogApi();
                            InputStreamRequestBody fileBody = new InputStreamRequestBody(inputStream, MediaType.parse("image/jpeg"));
                            fileBody.setLength(getFileSize(this,uri));

                            MultipartBody requestBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("token", "1c17b11693cb5ec63859b091c5b9c1b2")
                                    .addFormDataPart("image", uri.getPath(), fileBody)
                                    .build();
                            JsonObject result = JsonParser.parseString(apiBase.sendPost("https://hanana2.link/ottohub/EasyImages2.0/api/index.php", new ProgressedRequestBody(requestBody,(l, l1, v) -> {
                                runOnUiThread(()->alertDialog.setTitle(getString(R.string.loading)+(v*100)+"%"));
                            }))).getAsJsonObject();
                            if (result.get("code").getAsNumber().intValue() != 200) {
                                throw new IllegalStateException("Upload failed: " + result.get("message").getAsString());
                            }
                            runOnUiThread(()->{
                                alertDialog.dismiss();
                                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
                                UiUtil.insertTextAtCursor(findViewById(R.id.et_content),String.format(Locale.getDefault(),"\n![](%s)\n",result.get("url").getAsString()));
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(()->{
                                AlertUtil.showError(this, e.toString()).show();
                                alertDialog.dismiss();
                            });
                        }
                    });
                    thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this));
                    thread.start();
                }
            }
    );
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
                EmptyResult loadBlogResult = ApiUtil.getAppApi().getCreatorApi().save_blog(((EditText) findViewById(R.id.et_content)).getText().toString(), (l, l1, v1) ->runOnUiThread(()-> alertDialog.setTitle(getString(R.string.loading)+(v1*100)+"%")));
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
        findViewById(R.id.btn_localimg).setOnClickListener(v -> {
            filePickerLauncher.launch("image/*");
        });
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            AlertUtil.showYesNo(this, getString(R.string.upload_blog), getString(R.string.issure), (dialog, which) -> doUpload(alertDialog), null).show();
        });
    }

    private void doUpload(AlertDialog alertDialog) {
        AlertDialog alertDialog1 = AlertUtil.showLoading(this, getString(R.string.loading));
        alertDialog1.show();
        Thread thread1 = new Thread(() -> {
            SubmitBlogResult loadBlogResult = ApiUtil.getAppApi().getCreatorApi().submit_blog(((EditText) findViewById(R.id.et_title)).getText().toString(),((EditText) findViewById(R.id.et_content)).getText().toString(), (l, l1, v1) ->runOnUiThread(()-> alertDialog.setTitle(getString(R.string.loading)+(v1*100)+"%")));
            ApiUtil.throwApiError(loadBlogResult);
            runOnUiThread(() -> {
                alertDialog1.dismiss();
                AlertDialog alertDialog2 = AlertUtil.showMsg(this, getString(R.string.ok), loadBlogResult.if_add_experience == 1 ? "exp. +20" : "exp. +0");
                alertDialog2.show();
                alertDialog2.setOnDismissListener(dialog -> finish());
            });
        });
        thread1.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this){
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                super.uncaughtException(thread, ex);
                alertDialog1.dismiss();
            }
        });
        thread1.start();
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