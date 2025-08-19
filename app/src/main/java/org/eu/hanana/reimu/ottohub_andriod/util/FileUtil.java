package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class FileUtil {
    public static long getFileSize(Context context, Uri uri) {
        if (uri == null) return 0;

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        long size = 0;

        if (cursor != null) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            cursor.moveToFirst();
            size = cursor.getLong(sizeIndex);
            cursor.close();
        }
        return size;
    }

}
