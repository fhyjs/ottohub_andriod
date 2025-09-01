package org.eu.hanana.reimu.ottohub_andriod.util;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.OptIn;
import androidx.annotation.Px;
import androidx.fragment.app.Fragment;
import androidx.media3.common.C;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eu.hanana.reimu.ottohub_andriod.R;
import org.eu.hanana.reimu.ottohub_andriod.activity.BlogActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.ProfileActivity;
import org.eu.hanana.reimu.ottohub_andriod.activity.VideoPlayerActivity;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UiUtil {
    @OptIn(markerClass = UnstableApi.class)
    @AspectRatioFrameLayout.ResizeMode
    public static int getScaleTypeVideoInt(Context c) {
        var st = getScaleTypeVideo(c);
        if (st.equals("zoom")){
            return AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
        }
        if (st.equals("fit")){
            return AspectRatioFrameLayout.RESIZE_MODE_FIT;
        }
        if (st.equals("fill")){
            return AspectRatioFrameLayout.RESIZE_MODE_FILL;
        }
        return AspectRatioFrameLayout.RESIZE_MODE_FIT;
    }
    public static String getScaleTypeVideo(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getString("scale_type","auto");
    }
    public static int getAppWindowHeight(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+
            WindowMetrics metrics = activity.getWindowManager().getCurrentWindowMetrics();
            WindowInsets insets = metrics.getWindowInsets();

            // 去掉系统栏区域（状态栏、导航栏）
            int insetsHeight = insets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars()
            ).bottom + insets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars()
            ).top;

            return metrics.getBounds().height() - insetsHeight;
        } else {
            // API < 30
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            return dm.heightPixels;
        }
    }
    @OptIn(markerClass = UnstableApi.class)
    public static void openUrl(Context context, String url){
        if (url.toLowerCase(Locale.ROOT).contains("ottohub.cn")){
            var ottohubUrl = url.split("/");
            var ottohubOperation = ottohubUrl[ottohubUrl.length-2];
            var ottohubTarget = ottohubUrl[ottohubUrl.length-1];
            try {
                if (ottohubOperation.contains("b")){
                    Intent intent = new Intent(context, BlogActivity.class);
                    var data = new Bundle();
                    data.putInt(BlogActivity.KEY_BID,Integer.parseInt(ottohubTarget));
                    intent.putExtras(data);
                    context.startActivity(intent);
                    return;
                }else if (ottohubOperation.contains("v")){
                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                    var data = new Bundle();
                    data.putInt(VideoPlayerActivity.KEY_VID,Integer.parseInt(ottohubTarget));
                    intent.putExtras(data);
                    context.startActivity(intent);
                    return;
                }else if (ottohubOperation.contains("u")){
                    Intent intent = new Intent(context, ProfileActivity.class);
                    var data = new Bundle();
                    data.putInt(ProfileActivity.KEY_UID,Integer.parseInt(ottohubTarget));
                    intent.putExtras(data);
                    context.startActivity(intent);
                    return;
                }
            }catch (Exception ignored){}

        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            AlertUtil.showMsg(context,context.getString(R.string.open_url),context.getString(R.string.open_url_desc,url)).show();
        }
    }
    public static String toCssColor(int color){
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return "rgba(" + r + "," + g + "," + b + "," + (a / 255f) + ")";
    }
    public static boolean containsHtml(String text) {
        if (text == null) return false;

        // (?i) 忽略大小写, (?s) 让 . 匹配换行
        // 1. 成对标签：<tag ...>...</tag>
        // 2. 自闭合：<tag .../>
        // 3. 空标签写法：<img ...>（允许没有 / 结尾）
        String regex = "(?is)"
                + "<([a-z][a-z0-9]*)\\b[^>]*>.*?</\\1>"   // 成对标签
                + "|"
                + "<([a-z][a-z0-9]*)\\b[^>]*/>"           // 自闭合标签
                + "|"
                + "<(?is)\\b[^>]*>"; // 特殊空标签允许 > 结尾

        return java.util.regex.Pattern.compile(regex).matcher(text).find();
    }
    /**
     * 根据资源名称获取字符串
     * @param context Context
     * @param name 资源名，比如 "app_name"
     * @return 对应翻译的字符串，如果不存在返回 name
     */
    public static String getStringByName(Context context, String name) {
        int resId = context.getResources().getIdentifier(name, "string", context.getPackageName());
        if (resId != 0) {
            return context.getString(resId);
        } else {
            return name; // 资源不存在
        }
    }

    public static Activity getActivityFromView(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
    public static void getAllViews(View root, List<View> views) {

        views.add(root);

        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                getAllViews(group.getChildAt(i), views);
            }
        }
    }
    public static List<View> getAllViews(Fragment fragment) {
        View root = fragment.getView().getRootView();
        List<View> views = new ArrayList<>();
        getAllViews(root, views);
        return views;
    }
    public static List<View> getAllViews(View view) {
        View root = view.getRootView();
        List<View> views = new ArrayList<>();
        getAllViews(root, views);
        return views;
    }
    public static List<View> getAllViews(ViewGroup view) {
        View root = view;
        List<View> views = new ArrayList<>();
        getAllViews(root, views);
        return views;
    }
    public static List<View> getAllViews(Activity activity) {
        View root = activity.getWindow().getDecorView().getRootView();
        List<View> views = new ArrayList<>();
        getAllViews(root, views);
        return views;
    }
    public static void insertTextAtCursor(EditText et, String text) {
        int start = Math.max(et.getSelectionStart(), 0);
        int end = Math.max(et.getSelectionEnd(), 0);
        et.getText().replace(Math.min(start, end), Math.max(start, end), text, 0, text.length());
    }
    public static View clone(View view, View newView){
        // 保存状态
        byte[] savedState = saveViewState(view);


    // 恢复状态
        restoreViewState(newView, savedState, View.BaseSavedState.CREATOR);
        return newView;
    }
    public static void shareText(Context c, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain"); // 分享纯文本
        intent.putExtra(Intent.EXTRA_TEXT, text);

        // 弹出系统分享面板
        Intent chooser = Intent.createChooser(intent, "Send to...");
        c.startActivity(chooser);
    }
    /**
     * 序列化 View 状态为 byte[]
     */
    public static byte[] saveViewState(View view) {
        Parcelable state = saveViewState0(view);
        if (state == null) return null;

        Parcel parcel = Parcel.obtain();
        state.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }
    /**
     * 通过反射获取 View 的状态
     */
    public static Parcelable saveViewState0(View view) {
        if (view == null) return null;

        try {
            Method method = View.class.getDeclaredMethod("onSaveInstanceState");
            method.setAccessible(true);  // 允许访问 protected 方法
            return (Parcelable) method.invoke(view);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void restoreViewState(View view, Parcelable state) {
        if (view == null || state == null) return;

        try {
            Method method = View.class.getDeclaredMethod("onRestoreInstanceState", Parcelable.class);
            method.setAccessible(true);  // 关键：允许访问 protected 方法
            method.invoke(view, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 从 byte[] 恢复 View 状态
     */
    public static void restoreViewState(View view, byte[] bytes, Parcelable.Creator<?> creator) {
        if (bytes == null || creator == null) return;

        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        Parcelable state = (Parcelable) creator.createFromParcel(parcel);
        restoreViewState(view,state);
        parcel.recycle();
    }
    public static void slideUp(final View view) {
        view.animate()
                .translationY(-view.getHeight())
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    public static void slideDown(final View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.setTranslationY(-view.getHeight());

        view.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(300)
                .start();
    }
    public static String getPermissionDescription(Context ctx,String permission) {
        try {
            PermissionInfo info = ctx.getPackageManager().getPermissionInfo(permission, 0);
            CharSequence desc = info.loadDescription(ctx.getPackageManager());
            return desc != null ? desc.toString() : "无描述";
        } catch (PackageManager.NameNotFoundException e) {
            return "无描述";
        }
    }
    public static String getPermissionLabel(Context ctx,String permission) {
        try {
            PermissionInfo info = ctx.getPackageManager().getPermissionInfo(permission, 0);
            CharSequence label = info.loadLabel(ctx.getPackageManager());
            return label.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return permission; // 返回原始权限字符串
        }
    }
    @Px
    public static int dpToPx(Context context,int dp){
        // 你想设置的 dp 值
        float scale = context.getResources().getDisplayMetrics().density;
        // 四舍五入
        return (int) (dp * scale + 0.5f);
    }
    public static void loadImgToImageView(ImageView imageView, Uri img){
        Glide.with(imageView.getContext())
                .load(img)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into(imageView);
    }
    public static void loadImgToImageView(ImageView imageView, String img){
        Glide.with(imageView.getContext())
                .load(img)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into(imageView);
    }
    public static void loadImgToImageView(ImageView imageView, Bitmap img){
        Glide.with(imageView.getContext())
                .load(img)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into(imageView);
    }
    public static void loadImgToImageView(ImageView imageView, Drawable img){
        Glide.with(imageView.getContext())
                .load(img)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into(imageView);
    }
    public static void loadImgToImageView(ImageView imageView, URL img){
        Glide.with(imageView.getContext())
                .load(img)
                .placeholder(R.drawable.ic_launcher_background)  // 占位图
                .error(R.drawable.error_48px)        // 错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // 缓存策略
                .into(imageView);
    }
}
