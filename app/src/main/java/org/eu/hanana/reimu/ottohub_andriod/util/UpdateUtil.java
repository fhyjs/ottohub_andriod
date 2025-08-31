package org.eu.hanana.reimu.ottohub_andriod.util;

import com.google.gson.Gson;

import org.eu.hanana.reimu.ottohub_andriod.BuildConfig;
import org.eu.hanana.reimu.ottohub_andriod.data.api.UpdateDataEntry;

import java.nio.charset.StandardCharsets;

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
    public interface CheckResult{
        void onResult(boolean hasUpdate,UpdateDataEntry data);
    }
}
