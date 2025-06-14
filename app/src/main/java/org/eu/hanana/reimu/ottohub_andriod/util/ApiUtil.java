package org.eu.hanana.reimu.ottohub_andriod.util;

import org.eu.hanana.reimu.lib.ottohub.api.ApiResultBase;
import org.eu.hanana.reimu.lib.ottohub.api.OttohubApi;
import org.eu.hanana.reimu.lib.ottohub.api.im.NewMessageNumResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;

import lombok.Getter;

public class ApiUtil {
    @Getter
    private static int newMegCount;

    public static void throwApiError(ApiResultBase resultBase){
        if (!resultBase.isSuccess()) {
            throw new ApiException(resultBase.getMessage());
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
