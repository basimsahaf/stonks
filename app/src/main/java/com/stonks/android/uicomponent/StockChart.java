package com.stonks.android.uicomponent;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;
import androidx.core.widget.NestedScrollView;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.stonks.android.utility.Constants;
import com.stonks.android.utility.Formatters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StockChart extends LineChart {
    private ArrayList<TextView> priceListeners;
    private LineDataSet mainDataSet;
    private LineData mainData;
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

        this.mainDataSet = buildDataSet(Collections.singletonList(new Entry(1, 1)));
        this.mainData = new LineData(this.mainDataSet);

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
                        priceListeners.forEach(v -> v.setText(Formatters.formatPrice(e.getY())));
                        onScrub.accept((int) e.getX(), e.getY());
                    }

                    @Override
                    public void onNothingSelected() {}
                });
        this.setData(this.mainData);
    }

    public void setData(LineDataSet... data) {
        this.setData(new LineData(data));
    }

    public void setData(List<ILineDataSet> datasets) {
        this.setData(new LineData(datasets));
    }

    public void setLimitLine(float limit) {
        this.getAxisLeft().removeAllLimitLines();
        this.getAxisLeft().addLimitLine(getLimitLine(limit));
    }

    public static LineDataSet buildDataSet(List<Entry> data) {
        LineDataSet dataSet = new LineDataSet(data, "");

        dataSet.setLineWidth(3f);
        dataSet.setDrawValues(false);
        dataSet.setColor(Constants.primaryColor);

        // No indicators for individual data points
        dataSet.setDrawCircles(false);
        dataSet.setDrawCircleHole(false);

        // configure the scrub (a.k.a Highlight)
        dataSet.setHighLightColor(Color.WHITE);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setHighlightLineWidth(2f);

        // smoothen graph
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.1f);

        return dataSet;
    }

    public static LineDataSet buildIndicatorDataSet(List<Entry> data) {
        LineDataSet dataSet = new LineDataSet(data, "");

        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);
        dataSet.setColor(Color.GREEN);

        // No indicators for individual data points
        dataSet.setDrawCircles(false);
        dataSet.setDrawCircleHole(false);

        dataSet.setHighlightEnabled(false);

        // smoothen graph
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.1f);

        return dataSet;
    }

    private static LimitLine getLimitLine(float limit) {
        LimitLine line = new LimitLine(limit);
        line.setLineColor(Color.WHITE);
        line.setLineWidth(2f);
        line.enableDashedLine(5f, 10f, 10f);

        return line;
    }

    @FunctionalInterface
    public interface OnScrub {
        void accept(int x, float y);
    }

    public static class CustomGestureListener implements OnChartGestureListener {
        private final BarLineChartBase chart;
        private final NestedScrollView scrollView;
        private OnGestureEnded onGestureEnded;

        public CustomGestureListener(BarLineChartBase c, NestedScrollView s) {
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
