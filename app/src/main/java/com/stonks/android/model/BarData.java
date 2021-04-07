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

    @SerializedName("v")
    private Integer volume;

    private Integer endTimestamp;

    public BarData(int timestamp, float high, float low, float open, float close) {
        this.timestamp = timestamp;
        this.high = high;
        this.low = low;
        this.close = close;
        this.open = open;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Integer endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }

    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public String toString() {
        return "BarData{"
                + "timestamp="
                + timestamp
                + ", open="
                + open
                + ", close="
                + close
                + ", low="
                + low
                + ", high="
                + high
                + ", volume="
                + volume
                + ", endTimestamp="
                + endTimestamp
                + '}';
    }
}
