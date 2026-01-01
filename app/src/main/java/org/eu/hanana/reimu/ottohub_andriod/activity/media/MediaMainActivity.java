package org.eu.hanana.reimu.ottohub_andriod.activity.media;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.BaseActivity;
import org.eu.hanana.reimu.ottohub_andriod.util.ThemeUtil;

public class MediaMainActivity extends BaseActivity {
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navView;
    private ViewGroup navHeader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_media_main);
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.media_lib_title);
        toolbar.setSubtitle(R.string.media_lib_subtitle);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            return true;
        });

        // 创建汉堡菜单按钮
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(ThemeUtil.getTheme(this).getColorOnPrimary());
        // 显示左上角图标
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 菜单点击事件
        navView.setNavigationItemSelectedListener(item -> {
            onNavViewClick(item);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        navHeader = (ViewGroup) getLayoutInflater().inflate(R.layout.nav_header, getRoot(), false);
        navView.addHeaderView(navHeader);
    }

    private void onNavViewClick(MenuItem item) {

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected boolean needBottomPadding() {
        return false;
    }
}