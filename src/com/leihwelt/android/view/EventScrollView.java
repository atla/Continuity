package com.leihwelt.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class EventScrollView extends ScrollView {

    public static interface ScrollChangedListener {
        public void onScrollChanged(int l, int t, int oldl, int oldt);
    }

    private ScrollChangedListener listener = null;

    public EventScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EventScrollView(Context context) {
        super(context);
    }

    public EventScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (listener != null)
            listener.onScrollChanged(l, t, oldl, oldt);

    }

    public ScrollChangedListener getListener() {
        return listener;
    }

    public void setListener(ScrollChangedListener listener) {
        this.listener = listener;
    }

}
