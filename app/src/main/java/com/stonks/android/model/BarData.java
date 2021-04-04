package com.stonks.android.model;

import android.graphics.drawable.Drawable;
import com.github.mikephil.charting.data.CandleEntry;
import com.google.gson.annotations.SerializedName;

public class BarData extends CandleEntry {
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

    public BarData(float x, float shadowH, float shadowL, float open, float close) {
        super(x, shadowH, shadowL, open, close);
    }

    public BarData(float x, float shadowH, float shadowL, float open, float close, Object data) {
        super(x, shadowH, shadowL, open, close, data);
    }

    public BarData(float x, float shadowH, float shadowL, float open, float close, Drawable icon) {
        super(x, shadowH, shadowL, open, close, icon);
    }

    public BarData(
            float x,
            float shadowH,
            float shadowL,
            float open,
            float close,
            Drawable icon,
            Object data) {
        super(x, shadowH, shadowL, open, close, icon, data);
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

    @Override
    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    @Override
    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }

    @Override
    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    @Override
    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public CandleEntry toCandleEntry(int x) {
        return new CandleEntry(x, this.high, this.low, this.open, this.close);
    }
}
