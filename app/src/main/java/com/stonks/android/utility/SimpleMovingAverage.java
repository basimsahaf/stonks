package com.stonks.android.utility;

import com.github.mikephil.charting.data.Entry;
import com.stonks.android.model.BarData;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SimpleMovingAverage {
    final List<Entry> movingAverage;
    final LinkedList<BarData> chartData;
    int size;
    float sum;

    public SimpleMovingAverage(int size) {
        this.movingAverage = new ArrayList<>();
        this.chartData = new LinkedList<>();
        this.size = Math.max(size, 1);
        this.sum = 0f;
    }

    public void setData(List<BarData> barData) {
        this.movingAverage.clear();
        this.chartData.clear();
        this.sum = 0f;

        for (int i = 0; i < barData.size(); ++i) {
            this.add(barData.get(i), i);
        }
    }

    public void setData(List<BarData> barData, int size) {
        this.size = Math.max(size, 1);
        this.setData(barData);
    }

    void add(BarData bar, int index) {
        sum += bar.getOpen();
        chartData.offer(bar);

        if (index < size) {
            movingAverage.add(new Entry(chartData.size(), sum / chartData.size()));
            return;
        }

        sum -= chartData.poll().getOpen();
        movingAverage.add(new Entry(index, sum / size));
    }

    public List<Entry> getMovingAverage() {
        return movingAverage;
    }

    public int getSize() {
        return size;
    }
}
