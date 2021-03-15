package com.stonks.android.model;

import com.google.gson.annotations.SerializedName;

public class BarData {
    @SerializedName("t")
    private Integer timestamp;

    @SerializedName("o")
    private Float open;

    @SerializedName("c")
    private Float close;

    @SerializedName("l")
    private Float low;

    @SerializedName("h")
    private Float high;

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public Float getOpen() {
        return open;
    }

    public void setOpen(Float open) {
        this.open = open;
    }

    public Float getClose() {
        return close;
    }

    public void setClose(Float close) {
        this.close = close;
    }

    public Float getLow() {
        return low;
    }

    public void setLow(Float low) {
        this.low = low;
    }

    public Float getHigh() {
        return high;
    }

    public void setHigh(Float high) {
        this.high = high;
    }
}
