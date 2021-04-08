package com.stonks.android.utility;

import com.github.mikephil.charting.data.Entry;
import com.stonks.android.model.BarData;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void setSize(int size) {
        this.size = Math.max(size, 1);
        this.recalculateMovingAverage();
    }

    void add(BarData bar, int index) {
        sum += bar.getOpen();
        chartData.offer(bar);

        if (index < size) {
            movingAverage.add(new Entry(chartData.size(), sum / chartData.size()));
            return;
        }

        sum -= chartData.poll().getClose();
        movingAverage.add(new Entry(index, sum / size));
    }

    void recalculateMovingAverage() {
        this.movingAverage.clear();

        AtomicInteger tempSize = new AtomicInteger(0);
        this.sum = 0f;

        this.chartData.forEach(
                bar -> {
                    this.sum += bar.getOpen();

                    if (tempSize.incrementAndGet() <= size) {
                        this.movingAverage.add(
                                new Entry(tempSize.get(), this.sum / tempSize.get()));
                    } else {
                        this.sum -= this.chartData.getFirst().getOpen();
                        this.movingAverage.add(new Entry(tempSize.get(), this.sum / this.size));
                    }
                });
    }

    public List<Entry> getMovingAverage() {
        return movingAverage;
    }
}
