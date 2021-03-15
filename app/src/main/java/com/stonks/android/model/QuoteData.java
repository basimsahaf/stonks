package com.stonks.android.model;

import com.google.gson.annotations.SerializedName;

public class QuoteData {
    @SerializedName("52WkLow")
    private Float yearlyLow;

    @SerializedName("52WkHigh")
    private Float yearlyHigh;

    private Double peRatio;

    private Double divYield;

    private Integer totalVolume;

    private String symbol;

    public Float getYearlyLow() {
        return yearlyLow;
    }

    public void setYearlyLow(Float yearlyLow) {
        this.yearlyLow = yearlyLow;
    }

    public Float getYearlyHigh() {
        return yearlyHigh;
    }

    public void setYearlyHigh(Float yearlyHigh) {
        this.yearlyHigh = yearlyHigh;
    }

    public Double getPeRatio() {
        return peRatio;
    }

    public void setPeRatio(Double peRatio) {
        this.peRatio = peRatio;
    }

    public Double getDivYield() {
        return divYield;
    }

    public void setDivYield(Double divYield) {
        this.divYield = divYield;
    }

    public Integer getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Integer totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
