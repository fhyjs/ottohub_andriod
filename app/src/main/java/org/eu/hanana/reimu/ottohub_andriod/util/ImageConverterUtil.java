package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageConverterUtil {

    public static File convertImageFormat(
            Context context,
            int drawableResId, // 原始图片的资源ID (例如 R.drawable.my_png_image)
            String outputFileName, // 输出文件名 (例如 "converted_image.jpg")
            Bitmap.CompressFormat targetFormat, // 目标格式 (例如 Bitmap.CompressFormat.JPEG)
            int quality // 压缩质量 (0-100)，对PNG无效
    ) {
        FileOutputStream outputStream = null;
        Bitmap bitmap = null; // 在 finally 中回收
        try {
            // 1. 解码原始图片到 Bitmap
            bitmap = BitmapFactory.decodeResource(context.getResources(), drawableResId);
            if (bitmap == null) {
                Log.e("ImageConversion", "Failed to decode original image resource.");
                return null;
            }

            // 2. 创建输出文件和输出流
            File outputFile = new File(context.getCacheDir(), outputFileName);
            outputStream = new FileOutputStream(outputFile);

            // 3. 压缩并转换格式
            boolean success = bitmap.compress(targetFormat, quality, outputStream);

            if (success) {
                Log.d("ImageConversion", "Image converted successfully to: " + outputFile.getAbsolutePath());
                return outputFile;
            } else {
                Log.e("ImageConversion", "Failed to compress and convert image.");
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                return null;
            }

        } catch (IOException e) {
            Log.e("ImageConversion", "Error during image conversion", e);
            return null;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                Log.e("ImageConversion", "Error closing output stream", e);
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                // bitmap.recycle(); // 谨慎使用
            }
        }
    }
    public static byte[] convertImageFormatToByteArray(
            Context context,
            int drawableResId,
            Bitmap.CompressFormat targetFormat,
            int quality
    ) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        Bitmap bitmap = null;
        try {
            // 1. 解码原始图片到 Bitmap
            bitmap = BitmapFactory.decodeResource(context.getResources(), drawableResId);
            if (bitmap == null) {
                Log.e("ImageConversion", "Failed to decode original image resource.");
                return null;
            }

            // 2. 创建 ByteArrayOutputStream
            byteArrayOutputStream = new ByteArrayOutputStream();

            // 3. 压缩并转换格式到内存中的字节数组
            boolean success = bitmap.compress(targetFormat, quality, byteArrayOutputStream);

            if (success) {
                Log.d("ImageConversion", "Image converted successfully to byte array.");
                // 4. 获取字节数组
                return byteArrayOutputStream.toByteArray();
            } else {
                Log.e("ImageConversion", "Failed to compress and convert image to byte array.");
                return null;
            }

        } catch (Exception e) { // 捕获更通用的异常，因为Bitmap操作可能抛出其他运行时异常
            Log.e("ImageConversion", "Error during image conversion to byte array", e);
            return null;
        } finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close(); // 虽然对 ByteArrayOutputStream 无实际效果
                }
            } catch (IOException e) {
                Log.e("ImageConversion", "Error closing ByteArrayOutputStream", e);
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                 bitmap.recycle();
            }
        }
    }

    /**
     * Decodes an image from an InputStream, converts it to JPEG format,
     * and writes it to an OutputStream.
     *
     * @param inputStream The stream providing the original image data. This stream will be closed by this method.
     * @param outputStream The stream to write the converted JPEG image data to. This stream will NOT be closed by this method.
     * @param quality The quality of the JPEG compression (0-100).
     * @return True if conversion and writing were successful, false otherwise.
     */
    public static boolean convertStream(
            InputStream inputStream,
            OutputStream outputStream,
            int quality,
            Bitmap.CompressFormat format
    ) {
        Bitmap bitmap = null;
        try {
            // 1. Decode image from InputStream to Bitmap
            // Consider wrapping in BufferedInputStream if performance is critical for large streams
            // BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap == null) {
                Log.e("ImageConversion", "Failed to decode image from InputStream.");
                return false;
            }

            // 2. Compress Bitmap to JPEG and write to OutputStream
            boolean success = bitmap.compress(format, quality, outputStream);

            if (success) {
                Log.d("ImageConversion", "Image successfully converted to JPEG and written to OutputStream.");
            } else {
                Log.e("ImageConversion", "Failed to compress Bitmap to JPEG or write to OutputStream.");
            }
            return success;

        } catch (OutOfMemoryError oom) { // Specifically catch OOM for large images
            Log.e("ImageConversion", "OutOfMemoryError during image decoding or compression. Consider downsampling.", oom);
            return false;
        } catch (Exception e) { // Catch other potential exceptions like IOException from stream ops
            Log.e("ImageConversion", "Exception during image conversion or stream operations", e);
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close(); // Close the input stream
                }
            } catch (IOException e) {
                Log.e("ImageConversion", "Error closing InputStream", e);
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                 bitmap.recycle(); // Consider recycling if appropriate for your memory management strategy
            }
        }
    }
}