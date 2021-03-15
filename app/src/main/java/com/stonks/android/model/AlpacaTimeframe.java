package com.stonks.android.model;

public enum AlpacaTimeframe {
    MINUTE("minute"),
    MINUTES_5("5min"),
    MINUTES_15("15min"),
    DAY("day");

    private final String timeframe;

    AlpacaTimeframe(final String timeframe) {
        this.timeframe = timeframe;
    }

    @Override
    public String toString() {
        return this.timeframe;
    }
}
