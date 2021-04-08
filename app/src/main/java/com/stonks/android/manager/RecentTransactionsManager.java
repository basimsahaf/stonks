package com.stonks.android.manager;

import android.content.Context;
import com.stonks.android.model.LoginRepository;
import com.stonks.android.model.Transaction;
import com.stonks.android.model.TransactionFilters;
import com.stonks.android.model.TransactionMode;
import com.stonks.android.model.TransactionsListRow;
import com.stonks.android.storage.TransactionTable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class RecentTransactionsManager {
    private static RecentTransactionsManager recentTransactionsManager = null;
    private static TransactionTable transactionTable;
    private ArrayList<TransactionsListRow> transactions;
    private static TransactionFilters filters;
    private String username;

    private RecentTransactionsManager(Context context) {
        transactionTable = TransactionTable.getInstance(context);
        //username = LoginRepository.getInstance(context).getCurrentUser();
        username = "username"; // TODO: Remove when done testing
        filters = new TransactionFilters(username);
    }

    public static RecentTransactionsManager getInstance(Context context) {
        if (recentTransactionsManager == null) {
            recentTransactionsManager = new RecentTransactionsManager(context);
        }

        return recentTransactionsManager;
    }

    public ArrayList<TransactionsListRow> getTransactions() {
        ArrayList<Transaction> filteredTransactions = transactionTable.filterTransactions(filters);
        transactions = new ArrayList<>();
        if (filteredTransactions.size() == 0) {
            return this.transactions;
        }

        LocalDateTime currentDateRow = filteredTransactions.get(0).getCreatedAt();
        transactions.add(new TransactionsListRow(currentDateRow));

        for (Transaction transaction : filteredTransactions) {
            LocalDateTime createdAt = transaction.getCreatedAt();
            if (datesAreOnDifferentDays(createdAt, currentDateRow)) {
                transactions.add(new TransactionsListRow(createdAt));
                currentDateRow = createdAt;
            }

            transactions.add(new TransactionsListRow(transaction));
        }

        return this.transactions;
    }

    public ArrayList<String> getSymbols() {
        return transactionTable.getSymbols(username);
    }

    public void applySymbolFilter(String symbol) {
        filters.insertSymbol(symbol);
    }

    public void unapplySymbolFilter(String symbol) {
        filters.removeSymbol(symbol);
    }

    public void resetModeFilter() {
        filters.setMode(null);
    }

    public void applyModeFilter(TransactionMode mode) {
        filters.setMode(mode);
    }

    public void applyMinAmountFilter(int minAmount) {
        filters.setMinAmount(minAmount);
    }

    public void applyMaxAmountFilter(int maxAmount) {
        filters.setMaxAmount(maxAmount);
    }

    public void resetFilters() {
        filters.setMode(null);
        filters.setSymbols(new ArrayList<String>());
        filters.setMinAmount(TransactionFilters.UNINITIALIZED_AMOUNT);
        filters.setMaxAmount(TransactionFilters.UNINITIALIZED_AMOUNT);
    }

    public void resetMinMaxAmount() {
        filters.setMinAmount(TransactionFilters.UNINITIALIZED_AMOUNT);
        filters.setMaxAmount(TransactionFilters.UNINITIALIZED_AMOUNT);
    }

    private Boolean datesAreOnDifferentDays(LocalDateTime date1, LocalDateTime date2) {
        return date1.getDayOfYear() != date2.getDayOfYear() || date1.getYear() != date2.getYear();
    }
}
