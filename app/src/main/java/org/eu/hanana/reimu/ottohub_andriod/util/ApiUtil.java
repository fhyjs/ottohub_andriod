package org.eu.hanana.reimu.ottohub_andriod.util;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.MODE_WORLD_READABLE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;

import org.eu.hanana.reimu.lib.ottohub.api.ApiResultBase;
import org.eu.hanana.reimu.lib.ottohub.api.OttohubApi;
import org.eu.hanana.reimu.lib.ottohub.api.auth.LoginResult;
import org.eu.hanana.reimu.lib.ottohub.api.im.NewMessageNumResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static void logout() {
        if (!isLogin()) return;
        MyApp.getInstance().getSharedPreferences(SharedPreferencesKeys.Perf_Account_List, MODE_PRIVATE).edit().remove(getAppApi().getLoginResult().uid).apply();
        getAppApi().logout();
        MyApp.getInstance().getSharedPreferences(SharedPreferencesKeys.Perf_Auth, MODE_PRIVATE).edit().remove(SharedPreferencesKeys.Key_Username).remove(SharedPreferencesKeys.Key_Passwd).apply();
    }

    public static LoginResult login(String username, String password) {
        var result = getAppApi().getAuthApi().login(username, password);
        if (!result.isSuccess()) return result;
        SharedPreferences sharedPreferences = MyApp.getInstance().getSharedPreferences(SharedPreferencesKeys.Perf_Auth, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(SharedPreferencesKeys.Key_Username,username);
        edit.putString(SharedPreferencesKeys.Key_Passwd,password);
        edit.apply();

        sharedPreferences = MyApp.getInstance().getSharedPreferences(SharedPreferencesKeys.Perf_Account_List, MODE_PRIVATE);
        var gson = new Gson();
        var copy = new LoginResult(null,null,null,null,null);
        try {
            ClassUtil.copyFields(LoginResult.class,LoginResult.class,copy,result,false);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        copy.token=password;
        sharedPreferences.edit().putString(result.uid,gson.toJson(copy)).apply();
        return result;
    }
    public static Map<String,LoginResult> getAccounts(){
        Map<String,LoginResult> res = new HashMap<>();
        var gson = new Gson();
        MyApp.getInstance().getSharedPreferences(SharedPreferencesKeys.Perf_Account_List, MODE_PRIVATE).getAll().forEach((k,v)->{
            res.put(k,gson.fromJson(v.toString(),LoginResult.class));
        });
        return Map.copyOf(res);
    }

    public static void loginWithAlert(Activity ctx, int uid, String passwd,@Nullable CallbackLoginWithAlert callbackLoginWithAlert) {
        AlertDialog alertDialog = AlertUtil.showLoading(ctx, ctx.getString(R.string.auto_login));
        alertDialog.show();
        new Thread(() -> {
            LoginResult login = login(String.valueOf(uid), passwd);
            ctx.runOnUiThread(() -> {
                if (callbackLoginWithAlert != null) {
                    callbackLoginWithAlert.onLogin(login);
                }
                alertDialog.dismiss();
            });
        }).start();
    }
    public interface  CallbackLoginWithAlert{
        void onLogin(LoginResult loginResult);
    }
}
