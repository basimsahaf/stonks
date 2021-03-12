package com.stonks.android.adapter;

import android.graphics.RectF;
import com.robinhood.spark.SparkAdapter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class StockChartAdapter extends SparkAdapter {
    private final List<Float> yData;
    private final AtomicBoolean hasBaseline;
    private float baseline, maxX;

    public StockChartAdapter(List<Float> yData) {
        this(yData, -1);
    }

    public StockChartAdapter(List<Float> yData, float maxX) {
        this.yData = yData;
        this.hasBaseline = new AtomicBoolean(false);
        this.maxX = maxX;
    }

    @Override
    public int getCount() {
        return yData.size();
    }

    @Override
    public Object getItem(int index) {
        return yData.get(index);
    }

    @Override
    public float getY(int index) {
        return yData.get(index);
    }

    @Override
    public boolean hasBaseLine() {
        return true;
    }

    @Override
    public float getBaseLine() {
        return this.baseline;
    }

    @Override
    public RectF getDataBounds() {
        final int count = getCount();
        final boolean hasBaseLine = hasBaseLine();

        float minY = hasBaseLine ? getBaseLine() : Float.MAX_VALUE;
        float maxY = hasBaseLine ? minY : -Float.MAX_VALUE;
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            final float x = getX(i);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);

            final float y = getY(i);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        return new RectF(minX, minY, this.maxX == -1 ? maxX : this.maxX, maxY);
    }

    public void setBaseline(float baseline) {
        this.baseline = baseline;
        this.hasBaseline.set(true);
    }

    public void removeBaseline() {
        this.hasBaseline.set(false);
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    public void resetMaxX() {
        this.maxX = -1;
    }

    public void setData(List<Float> yData) {
        this.yData.clear();
        this.yData.addAll(yData);
    }
}
