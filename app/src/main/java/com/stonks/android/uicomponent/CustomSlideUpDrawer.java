package com.stonks.android.uicomponent;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class CustomSlideUpDrawer extends SlidingUpPanelLayout {
    public CustomSlideUpDrawer(Context context) {
        super(context);
    }

    public CustomSlideUpDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSlideUpDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setPanelHeight(0);
        setAnchorPoint(0.8f);
        setPanelState(PanelState.COLLAPSED);
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void openDrawer() {
        setPanelState(PanelState.ANCHORED);
    }

    public void closeDrawer() {
        smoothToBottom();
        setPanelState(PanelState.COLLAPSED);
    }
}
