package com.stonks.android.manager;

import android.content.Context;

import com.stonks.android.model.TransactionMode;
import com.stonks.android.storage.PortfolioTable;
import com.stonks.android.storage.TransactionTable;

import java.time.LocalDateTime;

public class BuySellManager {

    private static volatile BuySellManager buySellManager;
    private PortfolioTable portfolioTable;
    private TransactionTable transactionTable;

    private BuySellManager(Context context) {
        buySellManager =  BuySellManager.getInstance(context);
//        portfolioTable = PortfolioTable.
    }

    public static BuySellManager getInstance(Context context) {
        if(buySellManager == null) {
            buySellManager = new BuySellManager(context);
        }
        return buySellManager;
    }
    public void handleTransaction(String username, int numberOfShares, float price, LocalDateTime createdAt, TransactionMode mode) {


    }
}
