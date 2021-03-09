package com.stonks.android;

import android.animation.*;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.model.Transaction;
import com.stonks.android.uicomponent.SpeedDialExtendedFAB;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

public class StockActivity extends BaseActivity {
    private String symbol;
    private RecyclerView transactionList;
    private RecyclerView.Adapter transactionListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        RecyclerView.LayoutManager transactionListManager = new LinearLayoutManager(this);
        this.transactionList = findViewById(R.id.history_list);
        this.transactionList.setLayoutManager(transactionListManager);
        this.transactionListAdapter = new TransactionViewAdapter(this.getFakeTransactions());
        this.transactionList.setAdapter(this.transactionListAdapter);

        Intent intent = getIntent();
        this.symbol = intent.getStringExtra(getString(R.string.intent_extra_symbol));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(this.symbol);

        LinearLayout overlay = findViewById(R.id.screen_overlay);
        SpeedDialExtendedFAB tradeButton = findViewById(R.id.trade_button);

        tradeButton.setOnClickListener(
                v -> {
                    tradeButton.trigger(overlay);
                });
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
