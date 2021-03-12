package com.stonks.android;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.stonks.android.adapter.StockChartAdapter;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.model.Transaction;
import com.stonks.android.uicomponent.SpeedDialExtendedFab;
import com.stonks.android.uicomponent.StockChart;
import com.stonks.android.utility.Constants;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StockActivity extends BaseActivity {
    private String symbol;
    private RecyclerView transactionList;
    private RecyclerView.Adapter transactionListAdapter;
    private ConstraintLayout buyButtonContainer, sellButtonContainer, tryButtonContainer;
    private SpeedDialExtendedFab tradeButton;
    private LinearLayout overlay;
    private NestedScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        this.overlay = findViewById(R.id.screen_overlay);
        this.tradeButton = findViewById(R.id.trade_button);
        this.transactionList = findViewById(R.id.history_list);
        this.buyButtonContainer = findViewById(R.id.buy_button_container);
        this.sellButtonContainer = findViewById(R.id.sell_button_container);
        this.tryButtonContainer = findViewById(R.id.try_button_container);
        this.scrollView = findViewById(R.id.scroll_view);
        TextView currentPrice = findViewById(R.id.current_price);

        this.tradeButton.addToSpeedDial(this.buyButtonContainer);
        this.tradeButton.addToSpeedDial(this.sellButtonContainer);
        this.tradeButton.addToSpeedDial(this.tryButtonContainer);

        RecyclerView.LayoutManager transactionListManager = new LinearLayoutManager(this);
        this.transactionList.setLayoutManager(transactionListManager);
        this.transactionListAdapter = new TransactionViewAdapter(this.getFakeTransactions());
        this.transactionList.setAdapter(this.transactionListAdapter);

        Intent intent = getIntent();
        this.symbol = intent.getStringExtra(getString(R.string.intent_extra_symbol));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(this.symbol);

        StockChart chart = findViewById(R.id.stock_graph);

        chart.addValueListener(currentPrice);
        chart.setOnChartGestureListener(
                new StockChart.CustomGestureListener(chart, this.scrollView));

        LineDataSet dataSet = new LineDataSet(this.getFakeStockPrices(), "");

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

        LimitLine prevClose = new LimitLine(121.08f);
        prevClose.setLineColor(Color.WHITE);
        prevClose.setLineWidth(2f);
        prevClose.enableDashedLine(5f, 10f, 10f);

        chart.getAxisLeft().addLimitLine(prevClose);
        chart.setData(new LineData(dataSet));
        chart.invalidate();

        this.tradeButton.setOnClickListener(v -> tradeButton.trigger(this.overlay));
        this.overlay.setOnClickListener(v -> tradeButton.close(v));
    }

    @Override
    public void onBackPressed() {
        if (this.tradeButton.isExtended()) {
            super.onBackPressed();
        } else {
            this.tradeButton.close(this.overlay);
        }
    }

    private ArrayList<Entry> getFakeStockPrices() {
        ArrayList<Entry> list = new ArrayList<>();
        float[] prices = Constants.stockDataPoints;

        for (int i = 0; i < prices.length; i += 5) {
            list.add(new Entry(i, prices[i]));
        }

        return list;
    }

    private ArrayList<Transaction> getFakeTransactions() {
        ArrayList<Transaction> list = new ArrayList<>();

        list.add(
                new Transaction(
                        "UBER",
                        100,
                        56.92f,
                        "buy",
                        LocalDateTime.of(2020, Month.AUGUST, 19, 13, 14)));
        list.add(
                new Transaction(
                        "UBER",
                        268,
                        36.47f,
                        "buy",
                        LocalDateTime.of(2020, Month.AUGUST, 1, 9, 52)));

        return list;
    }
}
