package com.stonks.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.model.Transaction;
import com.stonks.android.uicomponent.SpeedDialExtendedFab;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

public class StockActivity extends BaseActivity {
    private String symbol;
    private RecyclerView transactionList;
    private RecyclerView.Adapter transactionListAdapter;
    private ConstraintLayout buyButtonContainer, sellButtonContainer, tryButtonContainer;
    private SpeedDialExtendedFab tradeButton;
    private LinearLayout overlay;

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

    private ArrayList<Transaction> getFakeTransactions() {
        ArrayList<Transaction> list = new ArrayList<>();

        list.add(new Transaction("UBER", 100, 56.92f, "buy", LocalDateTime.of(2020, Month.AUGUST, 19, 13, 14)));
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
