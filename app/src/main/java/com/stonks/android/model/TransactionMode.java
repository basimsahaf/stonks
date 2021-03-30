package com.stonks.android.model;

public enum TransactionMode {
    BUY,
    SELL;

    public String toString() {
        return this.name().toLowerCase();
    }

    public static TransactionMode fromString(String mode) {
        return TransactionMode.valueOf(mode.toUpperCase());
    }
}
