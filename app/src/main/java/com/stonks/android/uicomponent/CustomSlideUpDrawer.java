package com.stonks.android.uicomponent;

import android.content.Context;
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
        setPanelState(PanelState.COLLAPSED);
        setPanelHeight(0);
        setAnchorPoint(0.8f);
    }

    public void openDrawer() {
        setPanelState(PanelState.ANCHORED);
    }

    public void closeDrawer() {
        setPanelState(PanelState.HIDDEN);
    }
}
