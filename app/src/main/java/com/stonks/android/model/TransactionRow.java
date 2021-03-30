package com.stonks.android.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionRow {
    private final String username;
    private final String symbol;
    private final int quantity;
    private final float price;
    private final TransactionMode transactionType;
    private final LocalDateTime createdAt;

    public TransactionRow(
            String username,
            String symbol,
            int quantity,
            float price,
            TransactionMode transactionType,
            LocalDateTime createdAt) {
        this.username = username;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.transactionType = transactionType;
        this.createdAt = createdAt;
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

    public TransactionMode getTransactionType() {
        return transactionType;
    }

    public String getTransactionTypeString() {
        return transactionType.toString();
    }

    public String getCreatedAtString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return createdAt.format(formatter);
    }

    public static LocalDateTime getCreatedAtFromString(String createdAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(createdAt, formatter);
    }
}
