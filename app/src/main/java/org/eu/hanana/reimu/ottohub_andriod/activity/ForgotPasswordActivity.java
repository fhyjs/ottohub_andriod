package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

public class ForgotPasswordActivity extends BaseActivity {
    public static final String ACTION="a";
    public static final String ACTION_REGISTER="r";
    public static final String ACTION_RESET="b";
    public String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_forgot_password);
        action=getIntent().getStringExtra(ACTION);
        if (ACTION_REGISTER.equals(action)) {
            setTitle(R.string.register);
            ((Button) findViewById(R.id.btn_register)).setText(R.string.register);
        }else {
            setTitle(R.string.fogot_pw);
            ((Button) findViewById(R.id.btn_register)).setText(R.string.reset);
        }
        findViewById(R.id.btn_register).setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.et_email)).getText().toString();
            String pw = ((EditText) findViewById(R.id.et_password)).getText().toString();
            String cpw = ((EditText) findViewById(R.id.et_confirm_password)).getText().toString();
            String code = ((EditText) findViewById(R.id.et_code)).getText().toString();
            if (!pw.equals(cpw)){
                AlertUtil.showError(this, getString(R.string.pw_not_match)).show();
                return;
            }
            Thread thread = new Thread(() -> {
                if (ACTION_REGISTER.equals(action)) {
                    ApiUtil.throwApiError(ApiUtil.getAppApi().getAuthApi().register(email,pw,code));
                } else {
                    ApiUtil.throwApiError(ApiUtil.getAppApi().getAuthApi().passwordreset(email,pw,pw,code));
                }
                runOnUiThread(()->{
                    Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this));
            thread.start();
        });
        findViewById(R.id.btn_send_code).setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.et_email)).getText().toString();
            Thread thread = new Thread(() -> {
                if (ACTION_REGISTER.equals(action)) {
                    ApiUtil.throwApiError(ApiUtil.getAppApi().getAuthApi().register_verification_code(email));
                } else {
                    ApiUtil.throwApiError(ApiUtil.getAppApi().getAuthApi().passwordreset_verification_code(email));
                }
                runOnUiThread(()->{
                    Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
                });
            });
            thread.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(this));
            thread.start();
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