package org.eu.hanana.reimu.ottohub_andriod.activity;

import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.ARG_TYPE;
import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.TYPE_BLOG;
import static org.eu.hanana.reimu.ottohub_andriod.ui.comment.CommentFragmentBase.TYPE_VIDEO;
import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ACTION_SEARCH;
import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ARG_ACTION;
import static org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment.ARG_DATA;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.ui.blog.BlogListFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.settings.SettingsFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.user.UserListFragment;
import org.eu.hanana.reimu.ottohub_andriod.ui.video.VideoListFragment;

import java.util.Objects;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private static final String TYPE_USER = "user";
    private EditText searchEditText;
    private MaterialButton searchButton;

    @NonNull
    public String type = TYPE_VIDEO;
    private TabLayout searchType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getIntent().hasExtra(ARG_TYPE)) {
            type= Objects.requireNonNull(getIntent().getStringExtra(ARG_TYPE));
        }
        setTitle(R.string.search);

        // 初始化视图
        searchEditText = findViewById(R.id.searchContent);
        searchButton = findViewById(R.id.searchButton);
        searchType = findViewById(R.id.tlSearchType);

        // 设置搜索按钮点击事件
        searchButton.setOnClickListener(v -> performSearch());

        // 设置EditText的EditorAction监听器（回车键搜索）
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {

                performSearch();
                return true;
            }
            return false;
        });
        if (type.equals(TYPE_VIDEO)){
            searchType.selectTab(searchType.getTabAt(0));
        }else if (type.equals(TYPE_BLOG)){
            searchType.selectTab(searchType.getTabAt(1));
        }else if (type.equals(TYPE_USER)){
            searchType.selectTab(searchType.getTabAt(2));
        }
        searchType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (!searchEditText.getText().toString().trim().isEmpty()){
                    performSearch();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
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
    public void performSearch() {
        var fragment = (Fragment) new SettingsFragment();
        Log.d(TAG, "performSearch: "+searchEditText.getText().toString().trim());
        int selectedTabPosition = searchType.getSelectedTabPosition();
        if (selectedTabPosition==0){
            type=TYPE_VIDEO;
        }else if (selectedTabPosition==1){
            type=TYPE_BLOG;
        }else if (selectedTabPosition==2){
            type=TYPE_USER;
        }
        if (type.equals(TYPE_VIDEO)){
            fragment = new VideoListFragment();
            var args = new Bundle();
            args.putString(ARG_ACTION,ACTION_SEARCH);
            args.putString(ARG_DATA,searchEditText.getText().toString().trim());
            fragment.setArguments(args);

        }else if (type.equals(TYPE_BLOG)){
            fragment = new BlogListFragment();
            var args = new Bundle();
            args.putString(ARG_ACTION,ACTION_SEARCH);
            args.putString(ARG_DATA,searchEditText.getText().toString().trim());
            fragment.setArguments(args);

        }else if (type.equals(TYPE_USER)){
            fragment = new UserListFragment();
            var args = new Bundle();
            args.putString(ARG_ACTION,ACTION_SEARCH);
            args.putString(ARG_DATA,searchEditText.getText().toString().trim());
            fragment.setArguments(args);

    }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}