package com.stonks.android.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private final String username;
    private final LocalDateTime createdAt;
    private final String symbol;
    private final float price;
    private final int shares;
    private final TransactionMode transactionType;

    public Transaction(
            final String username,
            final String symbol,
            final int shares,
            final float price,
            final TransactionMode transactionType,
            final LocalDateTime createdAt) {
        this.username = username;
        this.createdAt = createdAt;
        this.symbol = symbol;
        this.price = price;
        this.shares = shares;
        this.transactionType = transactionType;
    }

    public Transaction(
            final String username,
            final String symbol,
            final int shares,
            final float price,
            final TransactionMode transactionType) {
        this.username = username;
        this.createdAt = null;
        this.symbol = symbol;
        this.price = price;
        this.shares = shares;
        this.transactionType = transactionType;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedAtString() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return createdAt.format(format);
    }

    public String getDateStringFromCreatedAt() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd MMM yy");
        return createdAt.format(format);
    }

    public String getTimeStringFromCreatedAt() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");
        // TODO: fix - timestamp not stored in EST
        // Subtracting 4 hours to render the correct time
        return createdAt.format(format);
    }

    public static LocalDateTime getCreatedAtFromString(String transactionDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(transactionDate, formatter);
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

    public float getTotalPrice() {
        return price * shares;
    }

    public TransactionMode getTransactionType() {
        return transactionType;
    }

    public String getTransactionTypeString() {
        return transactionType.toString();
    }
}
