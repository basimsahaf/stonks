package com.stonks.android.uicomponent;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import com.robinhood.spark.SparkView;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomSparkView extends SparkView {

    private final AtomicBoolean scrubLineOnRelease = new AtomicBoolean(false);

    public CustomSparkView(Context context) {
        super(context);
    }

    public CustomSparkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSparkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomSparkView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        Paint strokedBaseLine = getBaseLinePaint();
        strokedBaseLine.setStrokeWidth(5f);
        strokedBaseLine.setStrokeMiter(3f);
        strokedBaseLine.setStyle(Paint.Style.STROKE);
        strokedBaseLine.setStrokeCap(Paint.Cap.ROUND);
        strokedBaseLine.setStrokeJoin(Paint.Join.ROUND);
        strokedBaseLine.setPathEffect(new DashPathEffect(new float[] {10f, 20f}, 0f));
        strokedBaseLine.setColor(Color.WHITE);

        setBaseLinePaint(strokedBaseLine);
        setScrubEnabled(true);
    }

    @Override
    public void onScrubEnded() {
        if (!this.scrubLineOnRelease.get()) {
            super.onScrubEnded();
        }
    }

    public void keepScrubLineOnRelease() {
        this.scrubLineOnRelease.set(true);
    }
}
