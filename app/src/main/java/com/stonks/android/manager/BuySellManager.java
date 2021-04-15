package com.stonks.android.manager;

import android.content.Context;
import com.stonks.android.model.PortfolioItem;
import com.stonks.android.model.Transaction;
import com.stonks.android.model.TransactionMode;
import com.stonks.android.storage.PortfolioTable;
import com.stonks.android.storage.TransactionTable;
import com.stonks.android.storage.UserTable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class BuySellManager {

    private static BuySellManager buySellManager;
    private PortfolioTable portfolioTable;
    private TransactionTable transactionTable;
    private UserTable userTable;

    private BuySellManager(Context context) {
        portfolioTable = PortfolioTable.getInstance(context);
        transactionTable = TransactionTable.getInstance(context);
        userTable = UserTable.getInstance(context);
    }

    public static BuySellManager getInstance(Context context) {
        if (buySellManager == null) {
            buySellManager = new BuySellManager(context);
        }
        return buySellManager;
    }

    public boolean isTransactionValid(
            String username,
            String symbol,
            float amountAvailable,
            int quantity,
            float price,
            TransactionMode mode) {
        switch (mode) {
            case BUY:
                return quantity * price <= amountAvailable;
            case SELL:
                int totalStocks = getStocksOwnedBySymbol(username, symbol);
                return totalStocks <= quantity;
            default:
                return false;
        }
    }

    public boolean commitTransaction(
            String username,
            String stockName,
            int numberOfShares,
            float price,
            TransactionMode mode,
            LocalDateTime createdAt,
            float availableFunds) {

        Transaction transaction =
                new Transaction(username, stockName, numberOfShares, price, mode, createdAt);
        boolean result = transactionTable.addTransaction(transaction);

        if (result) {
            if (portfolioTable.checkIfPortfolioItemExists(username, stockName)) {
                ArrayList<PortfolioItem> portfolioItems =
                        portfolioTable.getPortfolioItemsBySymbol(username, stockName);
                PortfolioItem currentStock = portfolioItems.get(0);
                int newQuantity =
                        currentStock.getQuantity()
                                + (mode == TransactionMode.BUY ? numberOfShares : -numberOfShares);
                PortfolioItem updatedStock =
                        new PortfolioItem(
                                currentStock.getUsername(), currentStock.getSymbol(), newQuantity);
                result = portfolioTable.updatePortfolioItem(updatedStock);
            } else {
                PortfolioItem portfolioItem =
                        new PortfolioItem(username, stockName, numberOfShares);
                result = portfolioTable.addPortfolioItem(portfolioItem);
            }

            if (result) {
                float change =
                        availableFunds
                                + numberOfShares * price * (mode == TransactionMode.BUY ? -1 : 1);
                return userTable.updateTotalAmount(username, change);
            }
        }

        return false;
    }

    public int getStocksOwnedBySymbol(String username, String symbol) {
        ArrayList<PortfolioItem> stocksOwned =
                portfolioTable.getPortfolioItemsBySymbol(username, symbol);
        return stocksOwned.stream().mapToInt(PortfolioItem::getQuantity).sum();
    }
}
