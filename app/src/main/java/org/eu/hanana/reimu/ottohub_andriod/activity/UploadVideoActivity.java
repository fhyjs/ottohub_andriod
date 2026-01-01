package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.common.EmptyResult;
import org.eu.hanana.reimu.lib.ottohub.api.video.VideoResult;
import org.eu.hanana.reimu.lib.ottohub.util.InputStreamRequestBody;
import org.eu.hanana.reimu.lib.ottohub.util.ProgressedRequestBody;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.FileUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ImageConverterUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.VibrateUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadVideoActivity extends BaseActivity {
    @Nullable
    public Integer vid;
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
    private VideoResult data;

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
        if (getIntent().hasExtra("vid")){
            vid = getIntent().getIntExtra("vid",-1);
            setTitle(R.string.re_edit);
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
            addTagBtn(tag);
        });
        if (vid != null) {
            AlertDialog loading = AlertUtil.showLoading(this, "Loading");
            loading.show();
            Thread thread = new Thread(() -> {
                loadVData();
                runOnUiThread(()->{
                    loadVDataUi();
                    loading.dismiss();
                });
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this){
                @Override
                public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                    UploadVideoActivity.this.runOnUiThread(loading::dismiss);
                    super.uncaughtException(t, e);
                }
            });
            thread.start();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addTagBtn(String tag) {
        Button tagBtn = (Button) UiUtil.clone(findViewById(R.id.btn_tag_base),new MaterialButton(this));
        tagBtn.setTag("themed");
        ((LinearLayout) findViewById(R.id.ll_tag)).addView(tagBtn);
        tagBtn.setText("#"+ tag);
        tagBtn.setOnClickListener(v1 -> ((LinearLayout) findViewById(R.id.ll_tag)).removeView(v1));


    }
    private void resetView(View v) {
        // 恢复视觉状态
        v.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(150)
                .start();

        // 清理状态标记（如果你有用）
        v.setPressed(false);
        v.clearAnimation();
    }

    private int findTargetIndex(LinearLayout parent, float rawX) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            int[] loc = new int[2];
            child.getLocationOnScreen(loc);

            int centerX = loc[0] + child.getWidth() / 2;

            if (rawX < centerX) {
                return i;
            }
        }
        return parent.getChildCount();
    }
    private void handleReorder(View dragged, MotionEvent event) {
        LinearLayout parent = (LinearLayout) dragged.getParent();
        float rawX = event.getRawX();

        int from = parent.indexOfChild(dragged);
        int to = findTargetIndex(parent, rawX);

        if (to != -1 && to != from) {
            parent.removeView(dragged);
            parent.addView(dragged, to);
        }
    }
    private void finishDrag(View v) {
        v.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(150)
                .start();

        // TODO: 保存顺序 / 回调
    }
    private void loadVDataUi() {
        ((EditText) findViewById(R.id.et_title)).setText(data.title);
        ((EditText) findViewById(R.id.et_intro)).setText(data.intro);
        String[] vcv = getResources().getStringArray(R.array.vid_category_value);
        String[] vtv = getResources().getStringArray(R.array.vid_type_value);
        for (int i=0;i<vcv.length;i++) {
            var s = vcv[i];
            if (Integer.parseInt(s)==data.category) {
                categoryValue=data.category;
                ((AutoCompleteTextView) findViewById(R.id.actv_category)).setText(getResources().getStringArray(R.array.vid_category)[i],false);
                break;
            }
        }

        for (int i=0;i<vtv.length;i++) {
            var s = vtv[i];
            if (Integer.parseInt(s)==data.type) {
                typeValue=data.type;
                ((AutoCompleteTextView) findViewById(R.id.actv_type)).setText(getResources().getStringArray(R.array.vid_type)[i],false);
                break;
            }
        }
        for (String s : data.tag.split("#")) {
            if (s.isEmpty()) continue;
            addTagBtn(s);
        }
        UiUtil.loadImgToImageView(findViewById(R.id.ivThumbnail),data.cover_url);
    }

    private void loadVData() {
        data = ApiUtil.getAppApi().getVideoApi().get_video_detail(vid);
        ApiUtil.throwApiError(data);
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
            if (cover==null&&vid==null) {
                runOnUiThread(()->AlertUtil.showError(this,getString(R.string.no_cover)).show());
                alertDialog.dismiss();
                return;
            }
            try (
                    InputStream inputStream = vid==null?getContentResolver().openInputStream(cover): URI.create(data.cover_url).toURL().openStream();
                    InputStream inputStreamVideo = vid==null?getContentResolver().openInputStream(video): URI.create(data.video_url).toURL().openStream();
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
                .addFormDataPart("action", vid==null?"submit_video":"update_video")
                .addFormDataPart("token", ApiUtil.getAppApi().getLoginToken())
                .addFormDataPart("title", title)
                .addFormDataPart("intro", intro)
                .addFormDataPart("type", typeValue+"")
                .addFormDataPart("category", categoryValue+"")
                .addFormDataPart("tag", tags);
        if (vid!=null) builder.addFormDataPart("vid",vid+"");
        //)
        // 添加 InputStream 文件
        if (vid==null||this.video!=null) {
            builder.addFormDataPart("file_mp4", "video.mp4",
                    new InputStreamRequestBody(video, MediaType.get("video/mp4")));
        }
        if (vid==null||this.cover!=null) {
            builder.addFormDataPart("file_jpg", "cover.jpg",
                    new InputStreamRequestBody(cover, MediaType.get("image/jpeg")));
        }

        RequestBody requestBody = new ProgressedRequestBody(builder.build(), (l, l1, v) -> {
            runOnUiThread(() -> dialog.setTitle("Uploading..." + l / (float)size * 100 + "%"));
        });

        Request request = new Request.Builder()
                .url(vid==null?"https://api.ottohub.cn/module/creator/submit_video.php":"https://api.ottohub.cn/module/creator/update_video.php")
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
        response.close();
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