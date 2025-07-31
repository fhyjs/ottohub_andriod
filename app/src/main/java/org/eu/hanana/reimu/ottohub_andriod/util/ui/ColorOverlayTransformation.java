package org.eu.hanana.reimu.ottohub_andriod.util.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.HardwareRenderer;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class ColorOverlayTransformation extends BitmapTransformation {
    private static final String ID = "com.example.ColorOverlayTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private final int overlayColor;

    /**
     * @param overlayColor ARGB格式颜色，例如 0x80FF0000 是半透明红色
     */
    public ColorOverlayTransformation(int overlayColor) {
        this.overlayColor = overlayColor;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap bitmap = pool.get(toTransform.getWidth(), toTransform.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(toTransform, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(overlayColor);
        canvas.drawRect(0, 0, toTransform.getWidth(), toTransform.getHeight(), paint);

        return bitmap;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ColorOverlayTransformation) {
            return ((ColorOverlayTransformation) o).overlayColor == overlayColor;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + overlayColor;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        messageDigest.update(new byte[] {
            (byte) (overlayColor >> 24),
            (byte) (overlayColor >> 16),
            (byte) (overlayColor >> 8),
            (byte) overlayColor
        });
    }
}
