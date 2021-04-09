package com.stonks.android.utility;

import com.github.mikephil.charting.data.Entry;
import com.stonks.android.model.BarData;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WeightedMovingAverage {
    final List<Entry> movingAverage;
    final LinkedList<BarData> chartData;
    int size;

    public WeightedMovingAverage(int size) {
        this.movingAverage = new ArrayList<>();
        this.chartData = new LinkedList<>();
        this.size = Math.max(size, 1);
    }

    public void setData(List<BarData> barData) {
        this.movingAverage.clear();
        this.chartData.clear();

        this.calculate(barData);
    }

    public void setData(List<BarData> barData, int size) {
        this.size = Math.max(size, 1);
        this.calculate(barData);
    }

    void calculate(List<BarData> barData) {
        int d = size * (size + 1) / 2;

        for (int i = 0; i < barData.size(); ++i) {
            if (i < size) {
                this.movingAverage.add(new Entry(i, barData.get(i).getOpen()));
                continue;
            }

            float sum = 0f;
            for (int j = i - size + 1; j <= i; ++j) {
                int factor = (j - i + size);
                sum += barData.get(j).getOpen() * factor;
            }

            this.movingAverage.add(new Entry(i, sum / d));
        }
    }

    public List<Entry> getMovingAverage() {
        return movingAverage;
    }

    public int getSize() {
        return size;
    }
}
