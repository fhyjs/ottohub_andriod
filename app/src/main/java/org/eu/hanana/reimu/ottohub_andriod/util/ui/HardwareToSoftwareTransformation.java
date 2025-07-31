package org.eu.hanana.reimu.ottohub_andriod.util.ui;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class HardwareToSoftwareTransformation extends BitmapTransformation {
    private static final String ID = "com.example.HardwareToSoftwareTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    public HardwareToSoftwareTransformation() {
        // 无参构造
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        // 如果不是硬件位图，直接返回原图
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (toTransform.getConfig() != Bitmap.Config.HARDWARE) {
                return toTransform;
            }
        }

        // 复制为软件位图
        Bitmap softwareBitmap = toTransform.copy(Bitmap.Config.ARGB_8888, true);
        return softwareBitmap != null ? softwareBitmap : toTransform;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof HardwareToSoftwareTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
