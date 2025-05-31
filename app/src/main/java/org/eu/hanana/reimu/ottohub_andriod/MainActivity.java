package org.eu.hanana.reimu.ottohub_andriod;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.eu.hanana.reimu.ottohub_andriod.ui.blog.BlogFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment;

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
                    selectedFragment = BlogFragment.newInstance();
                } else if (itemId == R.id.nav_user) {
                    selectedFragment = null;
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