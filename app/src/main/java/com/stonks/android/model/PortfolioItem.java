package com.stonks.android.model;

import androidx.databinding.BaseObservable;

import com.stonks.android.BR;

public class PortfolioItem extends BaseObservable implements WebSocketObserver {
    private final String username;
    private final String symbol;
    private final int quantity;
    private float currentPrice;

    public PortfolioItem(String username, String symbol, int quantity) {
        this.username = username;
        this.symbol = symbol;
        this.quantity = quantity;
        this.currentPrice = 0.0f;
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


    public float getCurrentPrice() {
        return currentPrice;
    }

    @Override
    public void updateCurrentPrice(BarData data) {
        this.currentPrice = data.getClose();
        notifyChange();
    }

    public void setCurrentPrice(float data) {
        this.currentPrice = data;
    }
}
