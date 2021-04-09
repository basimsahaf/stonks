package com.stonks.android.model;

import java.util.ArrayList;

public class TransactionFilters {
    public static int UNINITIALIZED_AMOUNT = -1;

    private TransactionMode mode;
    private final String username;
    private int minAmount;
    private int maxAmount;
    private ArrayList<String> symbols;

    public TransactionFilters(String username) {
        // username filter is always applied
        this.username = username;
        this.minAmount = TransactionFilters.UNINITIALIZED_AMOUNT;
        this.maxAmount = TransactionFilters.UNINITIALIZED_AMOUNT;
        this.symbols = new ArrayList<>();
        this.mode = null;
    }

    public String getUsername() {
        return this.username;
    }

    public int getMinAmount() {
        return this.minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public TransactionMode getMode() {
        return this.mode;
    }

    public void setMode(TransactionMode mode) {
        this.mode = mode;
    }

    public ArrayList<String> getSymbols() {
        return this.symbols;
    }

    public String getSymbolsAsQueryString() {
        // ('SHOP', 'GOOGL',)
        Boolean firstElement = true;
        String symbolQuery = "(";

        for (String symbol : this.symbols) {
            if (firstElement) {
                symbolQuery += "'" + symbol + "'";
                firstElement = false;
                continue;
            }
            symbolQuery += ", '" + symbol + "'";
        }

        symbolQuery += ")";

        return symbolQuery;
    }

    public void insertSymbol(String symbol) {
        this.symbols.add(symbol);
    }

    public void removeSymbol(String symbol) {
        this.symbols.remove(symbol);
    }

    public void setSymbols(ArrayList<String> symbols) {
        this.symbols = symbols;
    }

    public Boolean isMinAmountFilterApplied() {
        return this.minAmount != TransactionFilters.UNINITIALIZED_AMOUNT;
    }

    public Boolean isMaxAmountFilterApplied() {
        return this.maxAmount != TransactionFilters.UNINITIALIZED_AMOUNT;
    }

    public Boolean isModeFilterApplied() {
        return this.mode != null;
    }

    public Boolean isSymbolFilterApplied() {
        return this.symbols.size() > 0;
    }
}
