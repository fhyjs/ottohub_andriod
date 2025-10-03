package org.eu.hanana.reimu.ottohub_andriod.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditAdapter.ARG_RESULT;
import static org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditAdapter.ARG_TARGET;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditAdapter;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

import java.util.Objects;

public class ImageAuditActivity extends BaseActivity {
    public static final String KEY_TYPE="type";
    public static final String KEY_DATA="data";
    public static final String KEY_URL="url";
    public String url;
    public String type;
    public int data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_audit);
        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            //Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
           // v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
           // return insets;
        //});
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getString(R.string.audit_title)+" : "+getString(R.string.image_viewer));

        if (getIntent().getExtras().containsKey(KEY_DATA)){
            data= Integer.parseInt(Objects.requireNonNull(getIntent().getExtras().getString(KEY_DATA)));
        }
        if (getIntent().getExtras().containsKey(KEY_TYPE)){
            type=getIntent().getExtras().getString(KEY_TYPE);
        }
        if (getIntent().getExtras().containsKey(KEY_URL)){
            url=getIntent().getExtras().getString(KEY_URL);
        }
        UiUtil.loadImgToImageView(findViewById(R.id.ivThumbnail),url);
        findViewById(R.id.ivThumbnail).setOnClickListener(v -> ImageViewActivity.start(this,url));
        findViewById(R.id.group_user).setVisibility(GONE);
        findViewById(R.id.group_audit).setVisibility(VISIBLE);
        findViewById(R.id.btn_approve).setOnClickListener(v -> {
            AlertUtil.showYesNo(this,getString(R.string.approve),getString(R.string.issure),(dialog, which) -> {
                Intent intent = new Intent();
                intent.putExtra(ARG_RESULT, true);
                intent.putExtra(AuditAdapter.ARG_TYPE, type);
                intent.putExtra(ARG_TARGET, data);
                setResult(RESULT_OK, intent);
                finish();
            },null).show();
        });
        findViewById(R.id.btn_reject).setOnClickListener(v -> {
            AlertUtil.showYesNo(this,getString(R.string.reject),getString(R.string.issure),(dialog, which) -> {
                Intent intent = new Intent();
                intent.putExtra(ARG_RESULT, false);
                intent.putExtra(AuditAdapter.ARG_TYPE, type);
                intent.putExtra(ARG_TARGET, data);
                setResult(RESULT_OK, intent);
                finish();
            },null).show();
        });
    }
}