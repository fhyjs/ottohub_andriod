package org.eu.hanana.reimu.ottohub_andriod.activity;

import static org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity.TYPE_PREVIEW;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;

import com.google.gson.Gson;

import org.eu.hanana.reimu.ottohub_andriod.BuildConfig;
import org.eu.hanana.reimu.ottohub_andriod.MainActivity;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.util.AlertUtil;

import java.util.Objects;

public class OttoHubDeepLinkActivity extends AppCompatActivity {

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otto_hub_deep_link);
        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            //Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
           // v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
           // return insets;
        //});

        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (uri != null) {
            String fullUrl = uri.toString();
            Intent target = null;
            if ("open".equals(uri.getHost())){
                target = new Intent(this, MainActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (getReferrer()!=null&&BuildConfig.APPLICATION_ID.equals(getReferrer().getHost())){
                        Toast.makeText(this, "Oh!Here is the OTTOHUB!", Toast.LENGTH_SHORT).show();
                        target=null;
                        finish();
                        return;
                    }
                }
            } else if ("blog".equals(uri.getHost())) {
                if (uri.getQueryParameter("bid")!=null){
                    int bid = Integer.parseInt(Objects.requireNonNull(uri.getQueryParameter("bid")));
                    target = new Intent(this, BlogActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(BlogActivity.KEY_BID,bid);
                    target.putExtras(bundle);

                }
            } else if ("video".equals(uri.getHost())) {
                if (uri.getQueryParameter("vid")!=null){
                    int v = Integer.parseInt(Objects.requireNonNull(uri.getQueryParameter("vid")));
                    target = new Intent(this, VideoPlayerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(VideoPlayerActivity.KEY_VID,v);
                    target.putExtras(bundle);

                }
            } else if ("user".equals(uri.getHost())) {
                if (uri.getQueryParameter("uid")!=null){
                    int v = Integer.parseInt(Objects.requireNonNull(uri.getQueryParameter("uid")));
                    target = new Intent(this, ProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(ProfileActivity.KEY_UID,v);
                    target.putExtras(bundle);

                }
            }
            if (target!=null) {
                startActivity(target);
                finish();
            }else {
                AlertUtil.showError(this,"No Launch Target For This URI!").setOnDismissListener(dialog -> {
                    finish();
                });
            }
        }

    }
}