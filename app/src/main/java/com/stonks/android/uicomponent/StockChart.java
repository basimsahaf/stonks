package com.stonks.android.uicomponent;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import androidx.core.widget.NestedScrollView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import java.util.ArrayList;
import java.util.Locale;

public class StockChart extends LineChart {
    private ArrayList<TextView> priceListeners;
    private OnScrub onScrub;

    public StockChart(Context context) {
        super(context);
    }

    public StockChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StockChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        this.priceListeners = new ArrayList<>();
        onScrub = (x, y) -> {};
    }

    public void addValueListener(TextView v) {
        if (this.priceListeners == null) {
            this.priceListeners = new ArrayList<>();
        }

        this.priceListeners.add(v);
    }

    public void setOnScrub(OnScrub onScrub) {
        this.onScrub = onScrub;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        this.setDrawGridBackground(false);
        this.setScaleEnabled(false);
        this.getLegend().setEnabled(false);
        this.setHighlightPerDragEnabled(false);

        this.getAxisLeft().setDrawGridLines(false);
        this.getAxisLeft().setDrawLabels(false);
        this.getAxisLeft().setDrawAxisLine(false);
        this.getAxisRight().setDrawGridLines(false);
        this.getAxisRight().setDrawLabels(false);
        this.getAxisRight().setDrawAxisLine(false);

        this.getXAxis().setDrawGridLines(false);
        this.getXAxis().setDrawLabels(false);
        this.getXAxis().setDrawAxisLine(false);

        this.setHapticFeedbackEnabled(true);
        this.setHorizontalScrollBarEnabled(false);
        this.setOnChartValueSelectedListener(
                new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {
                        priceListeners.forEach(
                                v -> v.setText(String.format(Locale.CANADA, "$%.2f", e.getY())));
                        onScrub.accept((int) e.getX(), e.getY());
                    }

                    @Override
                    public void onNothingSelected() {}
                });
    }

    @FunctionalInterface
    public interface OnScrub {
        void accept(int x, float y);
    }

    public static class CustomGestureListener implements OnChartGestureListener {
        private final StockChart chart;
        private final NestedScrollView scrollView;
        private OnGestureEnded onGestureEnded;

        public CustomGestureListener(StockChart c, NestedScrollView s) {
            chart = c;
            scrollView = s;
            onGestureEnded = () -> {};
        }

        public void setOnGestureEnded(OnGestureEnded onGestureEnded) {
            this.onGestureEnded = onGestureEnded;
        }

        @Override
        public void onChartGestureStart(
                MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}

        @Override
        public void onChartGestureEnd(
                MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            chart.highlightValue(null);
            chart.setHighlightPerDragEnabled(false);
            scrollView.requestDisallowInterceptTouchEvent(false);

            this.onGestureEnded.accept();
        }

        @Override
        public void onChartLongPressed(MotionEvent me) {
            scrollView.requestDisallowInterceptTouchEvent(true);
            Highlight h = chart.getHighlightByTouchPoint(me.getX(), me.getY());
            chart.highlightValue(h);

            chart.setHighlightPerDragEnabled(true);
            Log.d("Selected: ", me.getX() + ", " + me.getY());
        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {}

        @Override
        public void onChartSingleTapped(MotionEvent me) {}

        @Override
        public void onChartFling(
                MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}

        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}

        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {}

        @FunctionalInterface
        public interface OnGestureEnded {
            void accept();
        }
    }
}
