package com.stonks.android.model;

public class FavouriteStock {
    private final String username;
    private final String symbol;

    public FavouriteStock(String username, String symbol) {
        this.username = username;
        this.symbol = symbol;
    }

    public String getUsername() {
        return username;
    }

    public String getSymbol() {
        return symbol;
    }
}
