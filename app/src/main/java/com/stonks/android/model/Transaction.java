package com.stonks.android.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private LocalDateTime transactionDate;
    private String symbol;
    private float price;
    private int shares;
    private String transactionType; // buy or sell

    public Transaction(
            String symbol,
            int shares,
            float price,
            String transactionType,
            LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
        this.symbol = symbol;
        this.price = price;
        this.shares = shares;
        this.transactionType = transactionType;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getTransactionDateString() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd MMM yy");
        return transactionDate.format(format);
    }

    public String getTransactionTimeString() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");
        return transactionDate.format(format);
    }

    public String getSymbol() {
        return symbol;
    }

    public float getPrice() {
        return price;
    }

    public int getShares() {
        return shares;
    }

    public String getTransactionType() {
        return transactionType;
    }
}
