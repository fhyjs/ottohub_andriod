package org.eu.hanana.reimu.ottohub_andriod;

import static android.widget.Toast.LENGTH_SHORT;
import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.eu.hanana.reimu.lib.ottohub.api.auth.LoginResult;
import org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.LoginActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.blog.BlogListFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.user.ProfileFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.SharedPreferencesKeys;

public class MainActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable fetchMsgCountRunnable = new Runnable() {
        @Override
        public void run() {
            // 这里写你要定时执行的代码
            BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_user);

            // 设置数字
            badgeDrawable.setNumber(ApiUtil.getNewMegCount()); // 角标数字
            badgeDrawable.setVisible(ApiUtil.getNewMegCount()>0);

            // 继续循环执行
            handler.postDelayed(this, 2000); // 5秒后再执行
        }
    };
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(fetchMsgCountRunnable, 5000);  // 启动定时任务，延迟5秒执行
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(fetchMsgCountRunnable);  // 停止任务，避免内存泄漏
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,0);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        // 默认加载第一个 Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, VideoListFragment.newInstance())
                .commit();
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("auto_login",true)) {
            autoLogin();
        }else {
            Toast.makeText(this,R.string.auto_login_off,LENGTH_SHORT).show();
        }
    }
    private void autoLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferencesKeys.Perf_Auth, MODE_PRIVATE);
        var un = sharedPreferences.getString(SharedPreferencesKeys.Key_Username,null);
        var pw = sharedPreferences.getString(SharedPreferencesKeys.Key_Passwd,null);
        if (un==null||pw==null) return;
        var alertDialog = AlertUtil.showLoading(this, getString(R.string.auto_login));
        alertDialog.show();
        Thread thread = new Thread(() -> {
            LoginResult login = MyApp.getInstance().getOttohubApi().getAuthApi().login(un, pw);
            if (!login.isSuccess()){
                var msg = login.getMessage();
                if (msg.contains("error_password")){
                    msg=MyApp.getInstance().getString(R.string.error_password);
                }
                throw new IllegalStateException(msg);
            }
            ApiUtil.fetchMsgCount();
            runOnUiThread(()->{
                alertDialog.dismiss();
                Toast.makeText(MainActivity.this,R.string.welcome,LENGTH_SHORT).show();
                loginFinish();
            });
        });
        thread.setUncaughtExceptionHandler((t, e) -> runOnUiThread(()->{
            alertDialog.dismiss();
            AlertUtil.showMsg(MainActivity.this,getString(R.string.auto_login),getString(R.string.error)).show();
        }));
        thread.start();
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void loginFinish() {
        BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_user);

        // 设置数字
        badgeDrawable.setNumber(ApiUtil.getNewMegCount()); // 角标数字
        badgeDrawable.setVisible(ApiUtil.getNewMegCount()>0);

        // 设置位置（可选，默认右上角）
        badgeDrawable.setHorizontalOffset(6); // 调整水平偏移
        badgeDrawable.setVerticalOffset(-6);   // 调整垂直偏移

    }

    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_video) {
                    selectedFragment = VideoListFragment.newInstance();
                } else if (itemId == R.id.nav_blog) {
                    selectedFragment = BlogListFragment.newInstance();
                } else if (itemId == R.id.nav_user) {
                    if (MyApp.getInstance().getOttohubApi().getLoginToken()==null){
                        AlertUtil.showYesNo(this, getString(R.string.not_login), getString(R.string.login_in_now), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                        return false;
                    }else {
                        selectedFragment = ProfileFragment.newInstance(null);
                    }
                }

                if (selectedFragment != null) {
                    if (getSupportFragmentManager().findFragmentById(R.id.fragment_container)!=null&&selectedFragment.getClass()==getSupportFragmentManager().findFragmentById(R.id.fragment_container).getClass()){
                        return false;
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }else {
                    Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (fragmentById!=null)
                        getSupportFragmentManager().beginTransaction().remove(fragmentById).commit();
                }
                return true;
            };
}