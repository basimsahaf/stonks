package com.stonks.android.adapter;

import android.graphics.RectF;
import com.robinhood.spark.SparkAdapter;
import java.util.List;

public class StockChartAdapter extends SparkAdapter {
    private final List<Float> yData;
    private final float baseline, maxX;

    public StockChartAdapter(List<Float> yData, float baseline) {
        this.yData = yData;
        this.baseline = baseline;
        this.maxX = -1;
    }

    public StockChartAdapter(List<Float> yData, float baseline, float maxX) {
        this.yData = yData;
        this.baseline = baseline;
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
}
