package com.stonks.android.model;

public class TransactionRow {
    private final String username;
    private final String symbol;
    private final int quantity;
    private final float price;
    private final String transactionType; // buy or sell

    public TransactionRow(
            String username, String symbol, int quantity, float price, String transactionType) {
        this.username = username;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.transactionType = transactionType;
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

    public float getPrice() {
        return price;
    }

    public String getTransactionType() {
        return transactionType;
    }
}
