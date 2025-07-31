package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.ui.audit.AuditFragment;


public class AuditActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private final int[] tabTitles={R.string.avatar,R.string.cover,R.string.videos,R.string.blogs,R.string.comment};
    private final SparseArray<Fragment> fragmentMap = new SparseArray<>();
    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_audit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.audit_title);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Fragment frag;
                if (position==0){
                    frag = AuditFragment.newInstance(AuditFragment.TYPE_AVATAR);
                }else if (position==1){
                    frag = AuditFragment.newInstance(AuditFragment.TYPE_COVER);
                }else if (position==2){
                    frag = AuditFragment.newInstance(AuditFragment.TYPE_VIDEO);
                }else if (position==3){
                    frag = AuditFragment.newInstance(AuditFragment.TYPE_BLOG);
                }else if (position==4){
                    frag = AuditFragment.newInstance(AuditFragment.TYPE_COMMENT);
                }else
                    frag =  new Fragment();
                fragmentMap.put(position, frag);
                return frag;
            }

            @Override
            public int getItemCount() {
                return 5;
            }
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Fragment fragment = getFragment(position);
                if(fragment instanceof AuditFragment){
                    ((AuditFragment)fragment).refresh();
                }
            }
        });
        // 将 TabLayout 与 ViewPager2 绑定
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(String.format("%s %s",getText(tabTitles[position]),getText(R.string.audit)))).attach();
        Toast.makeText(this, R.string.under_development, Toast.LENGTH_SHORT).show();
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