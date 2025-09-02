package org.eu.hanana.reimu.ottohub_andriod.ui.base;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

import androidx.annotation.NonNull;

import com.kuaishou.akdanmaku.DanmakuConfig;
import com.kuaishou.akdanmaku.data.DanmakuItem;
import com.kuaishou.akdanmaku.data.DanmakuItemData;
import com.kuaishou.akdanmaku.render.DanmakuRenderer;
import com.kuaishou.akdanmaku.render.SimpleRenderer;
import com.kuaishou.akdanmaku.ui.DanmakuDisplayer;
import com.kuaishou.akdanmaku.utils.Size;

import java.util.HashMap;
import java.util.Map;

public class UnlockedDanmakuRender extends SimpleRenderer {

    private final TextPaint textPaint;
    private final TextPaint strokePaint;
    private final Paint debugPaint;
    private final Paint borderPaint;

    private static final int CANVAS_PADDING = 6;
    private static final int DEFAULT_DARK_COLOR = Color.argb(255, 0x22, 0x22, 0x22);
    private static final Map<Float, Float> sTextHeightCache = new HashMap<>();

    public UnlockedDanmakuRender() {
        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);

        strokePaint = new TextPaint();
        strokePaint.setTextSize(textPaint.getTextSize());
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(3f);
        strokePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        strokePaint.setAntiAlias(true);

        debugPaint = new Paint();
        debugPaint.setColor(Color.RED);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setAntiAlias(true);
        debugPaint.setStrokeWidth(6f);

        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(6f);
    }

    @Override
    public void updatePaint(DanmakuItem item, DanmakuDisplayer displayer, DanmakuConfig config) {
        DanmakuItemData danmakuItemData = item.getData();

        // 计算文字大小
        float textSize = danmakuItemData.getTextSize() * (displayer.getDensity() - 0.6f);
        textPaint.setTextSize(textSize * config.getTextSizeScale());
        textPaint.setColor(danmakuItemData.getTextColor() | Color.argb(255, 0, 0, 0));
        textPaint.setTypeface(config.getBold() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

        // 描边文字
        strokePaint.setTextSize(textPaint.getTextSize());
        strokePaint.setTypeface(textPaint.getTypeface());
        strokePaint.setColor(textPaint.getColor() == DEFAULT_DARK_COLOR ? Color.WHITE : Color.BLACK);
    }

    @Override
    public Size measure(DanmakuItem item, DanmakuDisplayer displayer, DanmakuConfig config) {
        updatePaint(item, displayer, config);
        DanmakuItemData danmakuItemData = item.getData();
        float textWidth = textPaint.measureText(danmakuItemData.getContent());
        float textHeight = getCacheHeight(textPaint);
        return new Size(Math.round(textWidth) + CANVAS_PADDING, Math.round(textHeight) + CANVAS_PADDING);
    }

    @Override
    public void draw(DanmakuItem item, Canvas canvas, DanmakuDisplayer displayer, DanmakuConfig config) {
        updatePaint(item, displayer, config);
        DanmakuItemData danmakuItemData = item.getData();

        float x = CANVAS_PADDING * 0.5f;
        float y = CANVAS_PADDING * 0.5f - textPaint.ascent();

        canvas.drawText(danmakuItemData.getContent(), x, y, strokePaint);
        canvas.drawText(danmakuItemData.getContent(), x, y, textPaint);

        if (danmakuItemData.getDanmakuStyle() == DanmakuItemData.DANMAKU_STYLE_SELF_SEND) {
            canvas.drawRect(0f, 0f, canvas.getWidth(), canvas.getHeight(), borderPaint);
        }
    }

    private static float getCacheHeight(Paint paint) {
        float textSize = paint.getTextSize();
        if (sTextHeightCache.containsKey(textSize)) {
            return sTextHeightCache.get(textSize);
        } else {
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float textHeight = fontMetrics.descent - fontMetrics.ascent + fontMetrics.leading;
            sTextHeightCache.put(textSize, textHeight);
            return textHeight;
        }
    }
}
