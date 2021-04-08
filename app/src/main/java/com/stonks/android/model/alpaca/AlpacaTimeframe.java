package com.stonks.android.model.alpaca;

public enum AlpacaTimeframe {
    MINUTE("minute"),
    MINUTES_5("5Min"),
    MINUTES_15("15Min"),
    DAY("day");

    private final String timeframe;

    AlpacaTimeframe(final String timeframe) {
        this.timeframe = timeframe;
    }

    @Override
    public String toString() {
        return this.timeframe;
    }

    public int toInt() {
        switch (timeframe) {
            default:
            case "minute":
                return 1;
            case "5Min":
                return 5;
            case "15Min":
                return 15;
            case "day":
                return 24 * 60;
        }
    }
}
