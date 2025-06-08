package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

public class CacheUtil {

    /**
     * 同步清除缓存（内部和外部缓存）
     * @param context 应用上下文
     * @return 是否全部成功删除
     */
    public static boolean clearCache(Context context) {
        boolean internalDeleted = deleteDir(context.getCacheDir());
        boolean externalDeleted = true;
        if (context.getExternalCacheDir() != null) {
            externalDeleted = deleteDir(context.getExternalCacheDir());
        }
        return internalDeleted && externalDeleted;
    }

    /**
     * 递归删除文件夹
     * @param dir 目录文件
     * @return 是否删除成功
     */
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    boolean success = deleteDir(child);
                    if (!success) return false;
                }
            }
        }
        return dir != null && dir.delete();
    }

    /**
     * 异步清除缓存，回调结果
     * @param context 应用上下文
     * @param callback 回调接口
     */
    public static void clearCacheAsync(Context context, CacheClearCallback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return clearCache(context);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (callback != null) {
                    callback.onClearFinished(result);
                }
            }
        }.execute();
    }

    public interface CacheClearCallback {
        void onClearFinished(boolean success);
    }
}
