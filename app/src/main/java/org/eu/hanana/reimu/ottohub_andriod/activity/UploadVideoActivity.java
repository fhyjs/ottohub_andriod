package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.common.EmptyResult;
import org.eu.hanana.reimu.lib.ottohub.util.InputStreamRequestBody;
import org.eu.hanana.reimu.lib.ottohub.util.ProgressedRequestBody;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.FileUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ImageConverterUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadVideoActivity extends BaseActivity {
    public final char[] punctuationMarks = new char[]{
            '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~', '—',
            '！', '“', '”', '＃', '￥', '％', '＆', '’', '（', '）', '＊', '＋', '，', '－', '．', '／', '：', '；', '＜', '＝', '＞', '？', '＠', '［', '＼', '］', '＾', '＿', '｀', '｛', '｜', '｝', '～',
            '。', '，', '！', '；', '：', '（', '）', '［', '］', '｛', '｝', '⋯', '﹐', '﹑', '。', '、', '〃', '〝', '〞', '〟', '﹔', '﹕', '﹖', '﹗', '「', '」', '『', '』', '【', '】', '〝', '〞',
            '\u2000', '\u2001', '\u2002', '\u2003', '\u2004', '\u2005', '\u2006', '\u2007', '\u2008', '\u2009', '\u200A', '\u200B', '\u2028', '\u2029', '\u202F', '\u205F', '\u2060'
    };
    protected int categoryValue=-1;
    protected int typeValue=-1;
    protected Uri cover;
    protected Uri video;
    private final ActivityResultLauncher<String> CoverFilePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    cover = uri;
                    UiUtil.loadImgToImageView(findViewById(R.id.ivThumbnail),uri);
                    AlertDialog alertDialog = AlertUtil.showLoading(this, getString(R.string.loading));
                    alertDialog.show();
                    new Thread(()-> {
                        var os = new ByteArrayOutputStream();
                        try (InputStream inputStream = getContentResolver().openInputStream(cover)) {
                            boolean b = ImageConverterUtil.convertStream(inputStream, os, 95, Bitmap.CompressFormat.JPEG);
                            if (!b) throw new IllegalStateException("Failed to convert image to jpg!");
                            if (os.size()>1000*1000) throw new IllegalStateException("Image is too big!!! ("+(os.size()/1000f)+"kb)");
                            runOnUiThread(()->{
                                ((TextView) findViewById(R.id.btn_upload_cover)).setText(String.format(Locale.getDefault(),"%s %s",getString(R.string.upload_cover),uri.getPath()));
                            });
                        }catch (Exception e){
                            runOnUiThread(()-> AlertUtil.showError(this,e.toString()).show());
                            return;
                        }finally {
                            runOnUiThread(alertDialog::dismiss);
                        }
                    }).start();
                }
            }
    );
    private final ActivityResultLauncher<String> VideoFilePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    AlertDialog alertDialog = AlertUtil.showLoading(this, getString(R.string.loading));
                    alertDialog.show();
                    new Thread(()-> {
                        try {
                            if (FileUtil.getFileSize(this,uri)> 200 * 1000 * 1000) throw new IllegalStateException(getString(R.string.file_too_big));
                            video = uri;
                            runOnUiThread(()->{
                                ((TextView) findViewById(R.id.btn_upload_video)).setText(String.format(Locale.getDefault(),"%s %s",getString(R.string.select_video),uri.getPath()));
                            });
                        }catch (Exception e){
                            runOnUiThread(()-> AlertUtil.showError(this,e.toString()).show());
                            return;
                        }finally {
                            runOnUiThread(alertDialog::dismiss);
                        }
                    }).start();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_video);
        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            //Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
           // v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
           // return insets;
        //});
        setTitle(R.string.upload_video);
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        AutoCompleteTextView category = findViewById(R.id.actv_category);
        String[] categories = getResources().getStringArray(R.array.vid_category);

        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(this,
                R.layout.dropdown_menu_popup_item, categories);
        category.setAdapter(adapterCategory);
        category.setOnClickListener(v -> category.showDropDown());
        category.setOnItemClickListener((parent, view, position, id) -> {
            categoryValue=Integer.parseInt(getResources().getStringArray(R.array.vid_category_value)[position]);
        });

        AutoCompleteTextView type = findViewById(R.id.actv_type);
        String[] types = getResources().getStringArray(R.array.vid_type);

        ArrayAdapter<String> adapterType = new ArrayAdapter<>(this,
                R.layout.dropdown_menu_popup_item, types);
        type.setAdapter(adapterType);
        type.setOnClickListener(v -> type.showDropDown());
        type.setOnItemClickListener((parent, view, position, id) -> {
            typeValue=Integer.parseInt(getResources().getStringArray(R.array.vid_type_value)[position]);
        });
        findViewById(R.id.btn_upload_cover).setOnClickListener(v -> {
            CoverFilePickerLauncher.launch("image/*");
        });
        findViewById(R.id.btn_upload_video).setOnClickListener(v -> {
            VideoFilePickerLauncher.launch("video/mp4");
        });
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            AlertUtil.showYesNo(this, getString(R.string.upload_video), getString(R.string.issure), (dialog, which) -> doUpload(), null).show();
        });
        findViewById(R.id.tv_trouble).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("ottohub://blog?bid=8263"))));
        findViewById(R.id.btn_add_tag).setOnClickListener(v -> {
            EditText tagEt = findViewById(R.id.et_tag);
            var tag = tagEt.getText().toString();
            for (char punctuationMark : punctuationMarks) {
                if (tag.contains(String.valueOf(punctuationMark))){
                    AlertUtil.showError(this,getString(R.string.unallow_char)).show();
                    return;
                }
            }
            if (tag.isEmpty()) return;
            tagEt.setText("");
            Button tagBtn = (Button) UiUtil.clone(findViewById(R.id.btn_tag_base),new MaterialButton(this));
            ((LinearLayout) findViewById(R.id.ll_tag)).addView(tagBtn);
            tagBtn.setText("#"+tag);
            tagBtn.setOnClickListener(v1 -> ((LinearLayout) findViewById(R.id.ll_tag)).removeView(v1));
        });
    }
    public String getTags(){
        LinearLayout tags = findViewById(R.id.ll_tag);
        var sb = new StringBuffer();
        for (int i = 0; i < tags.getChildCount(); i++) {
            Button tagBtn = (Button) tags.getChildAt(i);
            if (tagBtn.getId()==R.id.btn_tag_base) continue;
            sb.append(tagBtn.getText());
        }
        return sb.toString();
    }
    protected void doUpload() {
        AlertDialog alertDialog = AlertUtil.showLoading(this, getString(R.string.loading));
        alertDialog.show();
        new Thread(()-> {
            var os = new ByteArrayOutputStream();
            if (cover==null) {
                runOnUiThread(()->AlertUtil.showError(this,getString(R.string.no_cover)).show());
                alertDialog.dismiss();
                return;
            }
            try (
                    InputStream inputStream = getContentResolver().openInputStream(cover);
                    InputStream inputStreamVideo = getContentResolver().openInputStream(video);
            ) {
                if (((TextView) findViewById(R.id.et_title)).getText().toString().isEmpty()) throw new IllegalStateException(getString(R.string.empty_title));
                if (((TextView) findViewById(R.id.et_intro)).getText().toString().isEmpty()) throw new IllegalStateException(getString(R.string.empty_intro));
                if (typeValue==-1) throw new IllegalStateException(getString(R.string.empty_type));
                if (categoryValue==-1) throw new IllegalStateException(getString(R.string.empty_caterogy));
                runOnUiThread(()-> alertDialog.setTitle("Converting Image..."));
                boolean b = ImageConverterUtil.convertStream(inputStream, os, 95, Bitmap.CompressFormat.JPEG);
                if (!b) throw new IllegalStateException("Failed to convert image to jpg!");
                if (os.size()>1000*1000) throw new IllegalStateException("Image is too big!!! ("+(os.size()/1000f)+"kb)");
                var tags = getTags();
                if (tags.isEmpty()) throw new IllegalStateException("No tags was added!");
                doUpload_1(alertDialog,new ByteArrayInputStream(os.toByteArray()),inputStreamVideo,((TextView) findViewById(R.id.et_title)).getText().toString(),((TextView) findViewById(R.id.et_intro)).getText().toString(),tags);
            }catch (Exception e){
                runOnUiThread(()-> AlertUtil.showError(this,e.toString()).show());
                return;
            }finally {
                runOnUiThread(alertDialog::dismiss);
            }
        }).start();
    }

    private void doUpload_1(AlertDialog dialog, InputStream cover,InputStream video, String title, String intro,String tags) throws IOException {
        var size = video.available()+cover.available();
        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("action", "submit_video")
                .addFormDataPart("token", ApiUtil.getAppApi().getLoginToken())
                .addFormDataPart("title", title)
                .addFormDataPart("intro", intro)
                .addFormDataPart("type", typeValue+"")
                .addFormDataPart("category", categoryValue+"")
                .addFormDataPart("tag", tags);

        // 添加 InputStream 文件
        builder.addFormDataPart("file_mp4", "video.mp4",
                new InputStreamRequestBody(video, MediaType.get("video/mp4")));
        builder.addFormDataPart("file_jpg", "cover.jpg",
                new InputStreamRequestBody(cover, MediaType.get("image/jpeg")));

        RequestBody requestBody = new ProgressedRequestBody(builder.build(), (l, l1, v) -> {
            runOnUiThread(() -> dialog.setTitle("Uploading..." + l / (float)size * 100 + "%"));
        });

        Request request = new Request.Builder()
                .url("https://api.ottohub.cn/module/creator/submit_video.php")
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            String string = response.body().string();
            System.out.println("上传成功: " + string);
            EmptyResult emptyResult = new Gson().fromJson(string, EmptyResult.class);
            ApiUtil.throwApiError(emptyResult);
            runOnUiThread(()->{
                AlertDialog alertDialog = AlertUtil.showMsg(this, getString(R.string.ok), getString(R.string.upload_success));
                alertDialog.setOnDismissListener(dialog1 -> finish());
                alertDialog.show();
            });

        } else {
            throw new IOException("HTTP Failed: " + response.code());
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