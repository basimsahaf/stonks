package com.stonks.android.model;

public class PortfolioRow {
    private final String username;
    private final String symbol;
    private final int quantity;

    public PortfolioRow(String username, String symbol, int quantity) {
        this.username = username;
        this.symbol = symbol;
        this.quantity = quantity;
    }

    public String getUsername() {
        return username;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }
}
