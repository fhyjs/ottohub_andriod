package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.Px;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eu.hanana.reimu.ottohub_andriod.R;

import java.net.URL;

public class UiUtil {
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
