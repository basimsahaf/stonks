package com.stonks.android.model;

public class FavouriteStock {
    private final String username;
    private final String symbol;
    private final String createdAt;

    public FavouriteStock(String username, String symbol, String createdAt) {
        this.username = username;
        this.symbol = symbol;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
