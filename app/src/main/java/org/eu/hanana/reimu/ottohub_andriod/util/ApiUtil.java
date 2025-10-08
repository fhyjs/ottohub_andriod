package org.eu.hanana.reimu.ottohub_andriod.util;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eu.hanana.reimu.lib.ottohub.api.ApiResultBase;
import org.eu.hanana.reimu.lib.ottohub.api.OttohubApi;
import org.eu.hanana.reimu.lib.ottohub.api.auth.LoginResult;
import org.eu.hanana.reimu.lib.ottohub.api.im.NewMessageNumResult;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.MyAppApplicationLike;
import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.data.api.DataSerializerEntity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ApiUtil {
    private static final String TAG = "APIUtil";
    @Getter
    private static int newMegCount;
    private static Map<String,String> apiExceptionMessage = null;
    private static final OkHttpClient client = new OkHttpClient();
    protected static final Gson gson = new Gson();
    public static <T> T deserializationData(String data){
        DataSerializerEntity dataSerializerEntity = gson.fromJson(data, DataSerializerEntity.class);
        Class<T> clazz = null;
        try {
            clazz = (Class<T>) Class.forName(dataSerializerEntity.className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(dataSerializerEntity.data,clazz);
    }
    public static String serializeData(Object data){
        JsonElement dataJson = gson.toJsonTree(data);
        DataSerializerEntity dataSerializerEntity = new DataSerializerEntity(data.getClass().getName(), dataJson);
        return gson.toJson(dataJson);
    }
    public static void downloadFileToZip(ZipOutputStream zos, String url, String entryName) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("下载失败: " + response);
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new RuntimeException("响应体为空");
            }

            // 写入 ZIP 条目
            zos.putNextEntry(new ZipEntry(entryName));

            try (InputStream in = body.byteStream()) {
                byte[] buffer = new byte[8192]; // 8KB 缓冲区
                int len;
                while ((len = in.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }
            }

            zos.closeEntry();
        }
    }
    public static long getFileSize(String url) throws Exception {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .head()  // 使用 HEAD 请求，只返回响应头，不传输实体
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String length = response.header("Content-Length");
                if (length != null) {
                    return Long.parseLong(length);
                }
            }
        }
        return -1; // 获取失败
    }
    /**
     * 下载指定 URL 的文件，返回字节数组。
     * @param url 文件 URL
     * @return 文件内容的 byte[]
     * @throws Exception 下载过程中产生的任何异常都会抛出
     */
    public static byte[] downloadFile(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response);
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("响应体为空");
            }

            return body.bytes();
        }
    }
    public static void throwApiError(ApiResultBase resultBase){
        if (!resultBase.isSuccess()) {
            String message = resultBase.getMessage();
            checkErrorMessages(null);
            if (apiExceptionMessage!=null&&apiExceptionMessage.containsKey(message)) message=String.format(Locale.ROOT,"%s (%s)",apiExceptionMessage.get(message),message);
            ApiException apiException = new ApiException(message);
            Log.e(TAG, "throwApiError: ", apiException);
            throw apiException;
        }
    }
    public static OttohubApi getAppApi(){
        return MyAppApplicationLike.getInstance().getOttohubApi();
    }

    public static boolean isLogin() {
        return getAppApi().getLoginToken()!=null;
    }
    protected static void checkErrorMessages(@Nullable String c){
        if (apiExceptionMessage==null) {
            String country = LocaleUtil.getLocale(MyApp.getInstance().getApplicationContext()).toString().toLowerCase(Locale.ROOT).substring(0,2);
            if (c!=null) country=c;
            InputStream resourceAsStream = null;
            try {
                resourceAsStream = MyApp.getInstance().getAssets().open("message/api_exception_message_" + country + ".json");
            } catch (IOException e) {}
            if (resourceAsStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
                    String json = reader.lines().collect(Collectors.joining("\n"));
                    System.out.println(json);
                    JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                    apiExceptionMessage=new HashMap<>();
                    for (String s : jsonObject.keySet()) {
                        apiExceptionMessage.put(s,jsonObject.get(s).getAsString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("资源文件未找到！"+"message/api_exception_message_" + country + ".json");
                if (c==null){
                    checkErrorMessages("en");
                }
            }
        }
    }
    public static String downloadFileToBase64(String fileUrl) throws Exception {
        // 下载文件到字节数组
        byte[] fileBytes = downloadFileAsBytes(fileUrl);

        // 转换为 Base64
        String base64 = Base64.getEncoder().encodeToString(fileBytes);

        // 获取 MIME 类型
        String mimeType = getMimeType(fileUrl);

        // 生成 data URL
        return "data:" + mimeType + ";base64," + base64;
    }
    public static String getMimeType(String uri) {
        if (uri.contains("?")) {
            uri = uri.substring(0, uri.indexOf('?'));
        }
        String finalUri = uri;
        String extension = (String) Optional.ofNullable(uri).filter((u) -> {
            return u.contains(".");
        }).map((u) -> {
            return u.substring(finalUri.lastIndexOf(46) + 1);
        }).orElse("");

        try {
            String type = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0 及以上使用标准库
                type = Files.probeContentType(Paths.get("dummy." + extension));
            } else {
                // 旧版本使用 MIMETypeMap 兼容方案
                type = android.webkit.MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT));
            }

            // 如果 MIMETypeMap 返回 null，可再兜底一下
            if (type == null) {
                type = switch (extension.toLowerCase(Locale.ROOT)) {
                    case "jpg", "jpeg" -> "image/jpeg";
                    case "png" -> "image/png";
                    case "gif" -> "image/gif";
                    case "mp4" -> "video/mp4";
                    case "webm" -> "video/webm";
                    case "mp3" -> "audio/mpeg";
                    case "json" -> "application/json";
                    case "txt" -> "text/plain";
                    case "html" -> "text/html";
                    default -> "application/octet-stream";
                };
            }
            if (type.startsWith("text/")) {
                type = type + "; charset=UTF-8";
            }

            return type;
        } catch (IOException var3) {
            IOException e = var3;
            return "application/octet-stream";
        }
    }
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);

        for(int i = 0; i < length; ++i) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".charAt(RANDOM.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length())));
        }

        return sb.toString();
    }
    // 下载文件到字节数组
    public static byte[] downloadFileAsBytes(String fileUrl) throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }
    public static void fetchMsgCount() {
        if (!isLogin()) return;
        NewMessageNumResult newMessageNumResult = getAppApi().getMessageApi().new_message_num();
        throwApiError(newMessageNumResult);
        newMegCount =  newMessageNumResult.new_message_num;
    }

    public static void logout() {
        if (!isLogin()) return;
        if (PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance().getApplicationContext()).getBoolean("remove_account",true)) {
            MyApp.getInstance().getSharedPreferences(SharedPreferencesKeys.Perf_Account_List, MODE_PRIVATE).edit().remove(getAppApi().getLoginResult().uid).apply();
        }
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
