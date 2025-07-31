package org.eu.hanana.reimu.ottohub_andriod.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

public class ScrollInterceptFrameLayout extends FrameLayout {

    private NestedScrollView outerScrollView;
    private RecyclerView innerRecyclerView;

    public ScrollInterceptFrameLayout(Context context) {
        super(context);
    }

    public ScrollInterceptFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOuterScrollView(NestedScrollView outer) {
        this.outerScrollView = outer;
    }

    public void setInnerRecyclerView(RecyclerView inner) {
        this.innerRecyclerView = inner;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (outerScrollView != null) {
                // 判断外层是否还能继续滚动
                boolean canOuterScroll = outerScrollView.canScrollVertically(1);
                if (canOuterScroll) {
                    return true; // 由 FrameLayout 自己拦截，让 NestedScrollView 滚
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
}
