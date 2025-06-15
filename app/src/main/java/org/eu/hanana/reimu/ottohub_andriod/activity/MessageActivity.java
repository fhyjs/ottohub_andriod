package org.eu.hanana.reimu.ottohub_andriod.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.tabs.TabLayout;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.ui.message.MessageListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

public class MessageActivity extends AppCompatActivity {

    private TabLayout msgType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_message);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 启用返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        msgType = findViewById(R.id.tlMessageType);
        msgType.setEnabled(false);
        new Thread(this::init).start();
        setTitle(getString(R.string.loading));
    }
    protected void init(){
        ApiUtil.fetchMsgCount();
        runOnUiThread(this::initUi);
    }

    protected void initUi() {
        setTitle(getString(R.string.message)+" "+ ApiUtil.getNewMegCount()+"*");
        msgType.setEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, MessageListFragment.newInstance(MessageListFragment.TYPE_UNREAD)).commit();
        msgType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition()==0){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, MessageListFragment.newInstance(MessageListFragment.TYPE_UNREAD)).commit();
                }else if (tab.getPosition()==1){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, MessageListFragment.newInstance(MessageListFragment.TYPE_READ)).commit();
                }else if (tab.getPosition()==2){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, MessageListFragment.newInstance(MessageListFragment.TYPE_SENT)).commit();
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
}