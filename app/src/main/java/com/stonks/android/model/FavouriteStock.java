package com.stonks.android.model;

public class FavouriteStock {
    private final int id;
    private final String username;
    private final String symbol;

    public FavouriteStock(int id, String username, String symbol) {
        this.id = id;
        this.username = username;
        this.symbol = symbol;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getSymbol() {
        return symbol;
    }
}
