package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppVersionUtil {

    /**
     * 获取应用版本名，例如 "1.0.3"
     * @param context 上下文
     * @return 版本名，异常时返回 "未知版本"
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "未知版本";
        }
    }

    /**
     * 获取应用版本号（兼容Android P及以上）
     * @param context 上下文
     * @return 版本号，异常时返回 -1
     */
    public static int getVersionCode(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                return (int) pi.getLongVersionCode();
            } else {
                return pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }
    /** 获取包名 */
    public static String getPackageName(Context context) {
        return context.getPackageName();
    }


    /** 获取应用名称 */
    public static String getAppName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 0);
            return pm.getApplicationLabel(ai).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "未知应用";
        }
    }
}
