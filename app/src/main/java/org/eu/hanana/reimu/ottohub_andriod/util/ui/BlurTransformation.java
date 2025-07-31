package org.eu.hanana.reimu.ottohub_andriod.util.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import org.eu.hanana.reimu.ottohub_andriod.util.BlurUtils;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class BlurTransformation extends BitmapTransformation {
    private static final String ID = "org.eu.hanana.reimu.ottohub_andriod.util.ui.BlurTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private final int radius;
    private final Context ctx;

    public BlurTransformation(Context context, int radius) {
        this.ctx = context.getApplicationContext();
        this.radius = radius;
    }

    private Bitmap blurBitmap(Context context, Bitmap bitmap, int radius) {
        Bitmap inputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap.getWidth(), inputBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation inAlloc = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation outAlloc = Allocation.createFromBitmap(rs, outputBitmap);
        blur.setRadius(Math.min(25f, Math.max(0.1f, radius)));
        blur.setInput(inAlloc);
        blur.forEach(outAlloc);
        outAlloc.copyTo(outputBitmap);
        rs.destroy();

        return outputBitmap;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BlurTransformation && ((BlurTransformation) o).radius == radius;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + radius * 10;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        messageDigest.update(ByteBuffer.allocate(4).putInt(radius).array());
    }

    @NonNull
    @Override
    public String toString() {
        return "BlurTransformation(radius=" + radius + ")";
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
            Bitmap blurred = BlurUtils.blurBitmapRenderEffect(toTransform, radius);
            return blurred != null ? blurred : toTransform;
        } else {
            return blurBitmap(ctx, toTransform, radius);
        }
    }
}
