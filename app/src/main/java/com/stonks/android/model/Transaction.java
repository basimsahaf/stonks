package com.stonks.android.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private final LocalDateTime transactionDate;
    private final String symbol;
    private final float price;
    private final int shares;
    private final String transactionType; // buy or sell

    public Transaction(
            final String symbol,
            final int shares,
            final float price,
            final String transactionType,
            final LocalDateTime transactionDate) {
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
