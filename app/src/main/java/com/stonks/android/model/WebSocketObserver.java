package com.stonks.android.model;

public interface WebSocketObserver {
    void updateCurrentPrice(BarData newBar);
}
