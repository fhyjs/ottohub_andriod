package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import lombok.Setter;

public class TouchInterceptFrameLayout extends FrameLayout {

    /**
     * -- SETTER --
     *  设置是否拦截滑动事件
     */
    @Setter
    private boolean interceptMove = true; // 是否拦截滑动事件

    // 新增接口和成员变量
    public interface OnTouchListener {
        void onTouch(MotionEvent ev);
    }
    @Setter
    private OnTouchListener interceptTouchListener;
    @Setter
    private OnTouchListener touchListener;

    public TouchInterceptFrameLayout(@NonNull Context context) {
        super(context);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchListener != null) {
            touchListener.onTouch(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 通知监听者（如果有）
        if (interceptTouchListener != null) {
            interceptTouchListener.onTouch(ev);
        }

        // 只拦截滑动事件
        if (interceptMove && ev.getAction() == MotionEvent.ACTION_MOVE) {
            return true; // 拦截，不传给子 View
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        // 这里可以添加点击处理逻辑，或者保持空实现
        return true;
    }
}
