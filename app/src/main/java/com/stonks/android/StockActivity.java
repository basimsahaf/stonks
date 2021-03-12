package com.stonks.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.robinhood.spark.SparkView;
import com.stonks.android.adapter.StockChartAdapter;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.model.Transaction;
import com.stonks.android.uicomponent.SpeedDialExtendedFab;
import com.stonks.android.utility.Constants;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class StockActivity extends BaseActivity {
    private String symbol;
    private RecyclerView transactionList;
    private RecyclerView.Adapter<TransactionViewAdapter.ViewHolder> transactionListAdapter;
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

        SparkView sparkView = findViewById(R.id.stock_graph);
        sparkView.setAdapter(
                new StockChartAdapter(
                        this.getFakeStockPrices().stream()
                                .map(p -> p.second)
                                .collect(Collectors.toList()),
                        121.08f));
        sparkView.setScrubEnabled(true);
        sparkView.setScrubListener(
                value -> {
                    // disable scrolling when a value is selected
                    scrollView.requestDisallowInterceptTouchEvent(value != null);

                    if (value != null) {
                        currentPrice.setText("$" + value);
                    }
                });

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

    private ArrayList<Pair<Float, Float>> getFakeStockPrices() {
        ArrayList<Pair<Float, Float>> list = new ArrayList<>();
        float[] prices = Constants.stockDataPoints;

        for (int i = 0; i < prices.length; i += 5) {
            list.add(new Pair<>((float) i, prices[i]));
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
