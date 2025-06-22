package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import org.eu.hanana.reimu.ottohub_andriod.MainActivity;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.UiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LauncherActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 1234;
    private static final String TAG = "LauncherActivity";
    private ViewGroup container;

    // 你要请求的权限列表（动态构建）
    private List<String> getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        permissions.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
        permissions.add(Manifest.permission.INTERNET);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC);
        }
        return permissions;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_launcher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setTitle(R.string.launcher);
        container = findViewById(R.id.container);
        if (isAllGranted()){
            startMain();
            Log.i(TAG, "onCreate: All Granted!!!");
            return;
        }
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("permission_check",true)) {
            startMain();
            Log.w(TAG, "onCreate: Skip permission check!");
            return;
        }
        updateList(container);
        findViewById(R.id.btnSkip).setOnClickListener(v -> {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("permission_check",false).apply();
            startMain();
        });
    }

    private void updateList(ViewGroup container) {
        container.removeAllViews();
        AtomicBoolean allGranted = new AtomicBoolean(true);

        // 排序：未授权的排前面
        List<String> sortedPermissions = getRequiredPermissions().stream()
                .sorted((a, b) -> Boolean.compare(isGranted(a), isGranted(b))) // false < true
                .toList();

        for (String s : sortedPermissions) {
            ViewGroup inflate = (ViewGroup) getLayoutInflater().inflate(R.layout.card_permission, container, false);
            ((TextView) inflate.findViewById(R.id.tvName)).setText(UiUtil.getPermissionLabel(this, s));
            ((TextView) inflate.findViewById(R.id.tvInfo)).setText(UiUtil.getPermissionDescription(this, s));

            boolean granted = isGranted(s);
            ((CheckBox) inflate.findViewById(R.id.cbStatus)).setChecked(granted);

            if (!granted) {
                inflate.setOnClickListener(v -> {
                    ActivityCompat.requestPermissions(this, new String[]{s}, REQUEST_CODE_PERMISSIONS);
                });
            }

            container.addView(inflate);
            if (!granted) allGranted.set(false);
        }

        if (allGranted.get()) {
            startMain();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予
                Toast.makeText(this, "已获得权限", Toast.LENGTH_SHORT).show();
            } else {
                // 权限被拒绝
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
        updateList(container);
    }
    private void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("from_launcher",true);
        startActivity(intent);
        finish();
    }
    private List<String> getPermissionsNotGranted() {
        var res = new ArrayList<>(getRequiredPermissions());
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                res.remove(permission);
            }
        }
        return res;
    }
    private boolean isGranted(String permission){
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean isAllGranted(){
        return getPermissionsNotGranted().isEmpty();
    }
}