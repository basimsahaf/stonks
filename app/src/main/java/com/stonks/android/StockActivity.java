package com.stonks.android;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.stonks.android.adapter.StockChartAdapter;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.model.Transaction;
import com.stonks.android.uicomponent.CustomSlideUpDrawer;
import com.stonks.android.uicomponent.CustomSparkView;
import com.stonks.android.uicomponent.SpeedDialExtendedFab;
import com.stonks.android.utility.Constants;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

public class StockActivity extends BaseActivity {
    private static final String TAG = "DEBUG";
    private String symbol;
    private RecyclerView transactionList;
    private RecyclerView.Adapter<TransactionViewAdapter.ViewHolder> transactionListAdapter;
    private ConstraintLayout buyButtonContainer, sellButtonContainer, tryButtonContainer;
    private SpeedDialExtendedFab tradeButton;
    private LinearLayout overlay;
    private NestedScrollView scrollView;
    private FloatingActionButton tryButton;
    private CustomSlideUpDrawer customSlideUpDrawer;

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

        this.customSlideUpDrawer = findViewById(R.id.sliding_layout);

        this.tryButton = findViewById(R.id.try_button);
        this.tryButton.setOnClickListener(v -> {
            customSlideUpDrawer.openDrawer();
            Log.d(TAG, "HEIGHT: " + customSlideUpDrawer.getHeight());
            Log.d(TAG, "X y z: " + customSlideUpDrawer.getX() + " " + customSlideUpDrawer.getY() + " " + customSlideUpDrawer.getZ() );

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.sliding_drawer, new YourFragment());
            ft.commit();
        });

        RecyclerView.LayoutManager transactionListManager = new LinearLayoutManager(this);
        this.transactionList.setLayoutManager(transactionListManager);
        this.transactionListAdapter = new TransactionViewAdapter(this.getFakeTransactions());
        this.transactionList.setAdapter(this.transactionListAdapter);

        Intent intent = getIntent();
        this.symbol = intent.getStringExtra(getString(R.string.intent_extra_symbol));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(this.symbol);

        CustomSparkView sparkView = findViewById(R.id.stock_chart);
        StockChartAdapter dataAdapter =
                new StockChartAdapter(
                        this.getFakeStockPrices().stream()
                                .map(p -> p.second)
                                .collect(Collectors.toList()));
        dataAdapter.setBaseline(121.08f);

        sparkView.setAdapter(dataAdapter);
        sparkView.setScrubListener(
                value -> {
                    // disable scrolling when a value is selected
                    scrollView.requestDisallowInterceptTouchEvent(value != null);

                    if (value != null) {
                        currentPrice.setText(String.format(Locale.CANADA, "$%.2f", value));
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

    public static ArrayList<Transaction> getFakeTransactions() {
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
