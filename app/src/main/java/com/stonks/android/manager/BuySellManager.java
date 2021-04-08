package com.stonks.android.manager;

import android.content.Context;

import com.stonks.android.model.PortfolioItem;
import com.stonks.android.model.Transaction;
import com.stonks.android.model.TransactionMode;
import com.stonks.android.storage.PortfolioTable;
import com.stonks.android.storage.TransactionTable;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class BuySellManager {

    private static volatile BuySellManager buySellManager;
    private PortfolioTable portfolioTable;
    private TransactionTable transactionTable;

    private BuySellManager(Context context) {
        buySellManager =  BuySellManager.getInstance(context);
        portfolioTable = PortfolioTable.getInstance(context);
        transactionTable = TransactionTable.getInstance(context);
    }

    public static BuySellManager getInstance(Context context) {
        if(buySellManager == null) {
            buySellManager = new BuySellManager(context);
        }
        return buySellManager;
    }

    public boolean isTransactionValid(String username, String symbol, float amountAvailable, int quantity, float price, TransactionMode mode) {
        switch (mode) {
            case BUY:
                return quantity*price <= amountAvailable;

            case SELL:
                ArrayList<PortfolioItem> stocksOwned = portfolioTable.getPortfolioItemsBySymbol(username, symbol);
                int totalStocks = stocksOwned.stream().mapToInt(PortfolioItem::getQuantity).sum();
                return totalStocks <= quantity;

            default: return false;
        }
    }

    public boolean commitTransaction(String username, String stockName, int numberOfShares, float price, TransactionMode mode, LocalDateTime createdAt) {

        Transaction transaction = new Transaction(username, stockName, numberOfShares, price, mode, createdAt);
        boolean result = transactionTable.addTransaction(transaction);

        if (result) {
            PortfolioItem portfolioItem = new PortfolioItem(username, stockName, numberOfShares);
            return portfolioTable.addPortfolioItem(portfolioItem);
        }

        return false;

    }
}
