package org.eu.hanana.reimu.ottohub_andriod.util;

import android.util.Log;

import org.eu.hanana.reimu.lib.ottohub.api.ApiResultBase;
import org.eu.hanana.reimu.lib.ottohub.api.OttohubApi;
import org.eu.hanana.reimu.lib.ottohub.api.im.NewMessageNumResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;

import lombok.Getter;

public class ApiUtil {
    private static final String TAG = "APIUtil";
    @Getter
    private static int newMegCount;

    public static void throwApiError(ApiResultBase resultBase){
        if (!resultBase.isSuccess()) {
            ApiException apiException = new ApiException(resultBase.getMessage());
            Log.e(TAG, "throwApiError: ", apiException);
            throw apiException;
        }
    }
    public static OttohubApi getAppApi(){
        return MyApp.getInstance().getOttohubApi();
    }

    public static boolean isLogin() {
        return getAppApi().getLoginToken()!=null;
    }

    public static void fetchMsgCount() {
        if (!isLogin()) return;
        NewMessageNumResult newMessageNumResult = getAppApi().getMessageApi().new_message_num();
        throwApiError(newMessageNumResult);
        newMegCount =  newMessageNumResult.new_message_num;
    }
}
