package org.eu.hanana.reimu.ottohub_andriod.util;

import static org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity.TYPE_PREVIEW;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.blog.BlogResult;
import org.eu.hanana.reimu.ottohub_andriod.BuildConfig;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity;
import org.eu.hanana.reimu.ottohub_andriod.data.api.UpdateDataEntry;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UpdateUtil {
    public static String update_data_endpoint = "https://hanana2.link/ottohub/app/verinfo.json";
    public static void checkNow(CheckResult checkResult){
        try {
            String s = new String(ApiUtil.downloadFile(update_data_endpoint));
            var ude = new Gson().fromJson(s, UpdateDataEntry.class);
            ude.change_log=new String(ApiUtil.downloadFile(ude.change_log_url), StandardCharsets.UTF_8);
            var hasUpd = ude.version> BuildConfig.VERSION_CODE;
            checkResult.onResult(hasUpd,ude);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public static Thread getUpdaterChecker(Activity activity) {
        Thread threadCheckUpdate = new Thread(() -> UpdateUtil.checkNow((hasUpdate, data) -> {
            if (activity.isFinishing()||activity.isDestroyed()) return;
            if (!hasUpdate){
                activity.runOnUiThread(()->{
                    Toast.makeText(activity, activity.getString(R.string.update_no), Toast.LENGTH_SHORT).show();
                });
                return;
            }
            activity.runOnUiThread(()-> AlertUtil.showYesNo(activity,activity.getString(R.string.update_available),activity.getString(R.string.update_now),(dialog, which) -> {
                var br = new BlogResult();
                br.title="["+activity.getString(R.string.update_log)+"] "+data.version_str;
                br.content=String.format("<hr/><h1><a href=\"%s\">%s</a></h1><hr/>\n",data.download_url,activity.getString(R.string.update_now))+data.change_log;
                LocalDateTime now = LocalDateTime.now();
                br.time = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                br.username = "fhyjs";
                br.uid = 4384;
                br.avatar_url = "https://cdn.ottohub.cn/user/user_avatar/user_avatar_4384.jpg";
                Intent intent = new Intent(activity, BlogActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(BlogActivity.KEY_BID,0);
                bundle.putString(BlogActivity.KEY_DATA,new Gson().toJson(br));
                bundle.putString(BlogActivity.KEY_TYPE,TYPE_PREVIEW);
                intent.putExtras(bundle);
                activity.startActivity(intent);
            },null).show());
        }));
        threadCheckUpdate.setUncaughtExceptionHandler(new AlertUtil.ThreadAlert(activity));
        return threadCheckUpdate;
    }
    public interface CheckResult{
        void onResult(boolean hasUpdate,UpdateDataEntry data);
    }
}
