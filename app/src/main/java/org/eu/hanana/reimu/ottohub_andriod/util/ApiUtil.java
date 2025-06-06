package org.eu.hanana.reimu.ottohub_andriod.util;

import org.eu.hanana.reimu.lib.ottohub.api.ApiResultBase;
import org.eu.hanana.reimu.lib.ottohub.api.OttohubApi;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;

public class ApiUtil {
    public static void throwApiError(ApiResultBase resultBase){
        if (!resultBase.isSuccess())
            throw new ApiException(resultBase.getMessage());
    }
    public static OttohubApi getAppApi(){
        return MyApp.getInstance().getOttohubApi();
    }
}
