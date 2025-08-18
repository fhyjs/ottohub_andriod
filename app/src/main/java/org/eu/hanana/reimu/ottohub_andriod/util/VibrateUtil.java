package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrateUtil {
    public static void vibrate(Context context, long millis) {
        Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vib == null || !vib.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vib.vibrate(millis);
        }
    }
}
