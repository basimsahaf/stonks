package com.stonks.android.uicomponent;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.stonks.android.R;
import com.stonks.android.utility.Constants;
import java.util.List;

public class CandleChart extends CandleStickChart {
    private StockChart.OnScrub onScrub;

    public CandleChart(Context context) {
        super(context);
    }

    public CandleChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CandleChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        onScrub = (x, y) -> {};
    }

    public void setOnScrub(StockChart.OnScrub onScrub) {
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

        this.getDescription().setEnabled(false);

        this.getXAxis().setDrawGridLines(false);
        this.getXAxis().setDrawLabels(false);
        this.getXAxis().setDrawAxisLine(false);

        this.setHapticFeedbackEnabled(true);
        this.setHorizontalScrollBarEnabled(false);
        this.setOnChartValueSelectedListener(
                new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {
                        onScrub.accept((int) e.getX(), e.getY());
                    }

                    @Override
                    public void onNothingSelected() {}
                });
    }

    public void setData(List<CandleEntry> data) {
        CandleDataSet dataSet = new CandleDataSet(data, "");

        dataSet.setColor(Constants.primaryColor);
        dataSet.setShadowColor(Color.GRAY);
        dataSet.setShadowWidth(0.5f);
        dataSet.setDecreasingColor(ContextCompat.getColor(getContext(), R.color.red));
        dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
        dataSet.setIncreasingColor(ContextCompat.getColor(getContext(), R.color.green));
        dataSet.setIncreasingPaintStyle(Paint.Style.FILL);
        dataSet.setBarSpace(0.3f);
        dataSet.setNeutralColor(ContextCompat.getColor(getContext(), R.color.green));
        dataSet.setDrawValues(false);
        dataSet.setShowCandleBar(true);

        // configure the scrub (a.k.a Highlight)
        dataSet.setHighLightColor(Color.WHITE);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setHighlightLineWidth(1f);

        this.setData(new CandleData(dataSet));
        this.invalidate();
    }

    public void setLimitLine(float limit) {
        this.getAxisLeft().removeAllLimitLines();
        this.getAxisLeft().addLimitLine(getLimitLine(limit));
    }

    private static LimitLine getLimitLine(float limit) {
        LimitLine line = new LimitLine(limit);
        line.setLineColor(Color.WHITE);
        line.setLineWidth(2f);
        line.enableDashedLine(5f, 10f, 10f);

        return line;
    }
}
