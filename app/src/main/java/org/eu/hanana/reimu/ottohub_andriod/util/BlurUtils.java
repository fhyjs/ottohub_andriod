package org.eu.hanana.reimu.ottohub_andriod.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.HardwareRenderer;
import android.graphics.PixelFormat;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.hardware.HardwareBuffer;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class BlurUtils {

    /**
     * 使用 RenderEffect 和 HardwareRenderer 离屏渲染模糊 Bitmap，API 31+可用。
     * @param srcBitmap 原始Bitmap
     * @param radius 模糊半径，建议10~25
     * @return 模糊后的Bitmap，失败返回null
     */
    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static Bitmap blurBitmapRenderEffect(Bitmap srcBitmap, float radius) {
        if (srcBitmap == null || radius <= 0) return null;

        try {
            final int width = srcBitmap.getWidth();
            final int height = srcBitmap.getHeight();

            // 1. 创建 ImageReader 作为渲染目标
            ImageReader imageReader = ImageReader.newInstance(
                    width, height,
                    PixelFormat.RGBA_8888, 2,
                    HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE | HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
            );

            // 2. 创建 RenderNode 并设置大小
            android.graphics.RenderNode renderNode = new android.graphics.RenderNode("BlurRenderNode");
            renderNode.setPosition(0, 0, width, height);

            // 3. 创建 HardwareRenderer 并绑定 ImageReader 的 Surface
            HardwareRenderer hardwareRenderer = new HardwareRenderer();
            hardwareRenderer.setSurface(imageReader.getSurface());
            hardwareRenderer.setContentRoot(renderNode);

            // 4. 创建模糊 RenderEffect
            RenderEffect blurEffect = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP);
            renderNode.setRenderEffect(blurEffect);

            // 5. 开始录制绘制指令，绘制原始 Bitmap
            Canvas canvas = renderNode.beginRecording();
            canvas.drawBitmap(srcBitmap, 0f, 0f, null);
            renderNode.endRecording();

            // 6. 创建并同步渲染请求
            hardwareRenderer.createRenderRequest()
                    .setWaitForPresent(true)
                    .syncAndDraw();

            // 7. 获取渲染结果 Image
            Image image = imageReader.acquireNextImage();
            if (image == null) {
                cleanup(imageReader, renderNode, hardwareRenderer, null);
                return null;
            }

            HardwareBuffer hardwareBuffer = image.getHardwareBuffer();
            if (hardwareBuffer == null) {
                image.close();
                cleanup(imageReader, renderNode, hardwareRenderer, null);
                return null;
            }

            // 8. 把 HardwareBuffer 包装成 Bitmap
            Bitmap blurredBitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, null);

            // 9. 释放资源
            hardwareBuffer.close();
            image.close();
            cleanup(imageReader, renderNode, hardwareRenderer, null);

            return blurredBitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void cleanup(ImageReader imageReader, android.graphics.RenderNode renderNode, HardwareRenderer hardwareRenderer, Bitmap bitmap) {
        if (imageReader != null) imageReader.close();
        if (renderNode != null) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            renderNode.discardDisplayList();
        }
        if (hardwareRenderer != null) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hardwareRenderer.destroy();
        }
        if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
    }
}
