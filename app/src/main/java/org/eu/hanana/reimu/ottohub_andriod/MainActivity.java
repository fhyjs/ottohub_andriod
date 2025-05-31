package org.eu.hanana.reimu.ottohub_andriod;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.LoginActivity;
import org.eu.hanana.reimu.ottohub_andriod.ui.blog.BlogListFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.user.ProfileFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
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
                        selectedFragment = ProfileFragment.newInstance();
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