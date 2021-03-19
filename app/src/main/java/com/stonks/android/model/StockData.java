package com.stonks.android.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public class StockData implements Serializable {
    private final String symbol;
    private final String companyName;
    private final Float currentPrice;
    private final Float open;
    private final Integer volume;
    private final Float low;
    private final Float high;
    private final Float yearlyLow;
    private final Float yearlyHigh;
    private final Double peRatio;
    private final Double divYield;
    private final String description;
    private final List<BarData> graphData;

    public StockData(List<BarData> barData, QuoteData quoteData) {
        this.symbol = quoteData.getSymbol();
        switch (this.symbol) {
            case "UBER":
                this.companyName = "Uber";
                break;
            case "SHOP":
                this.companyName = "Shopify";
                break;
            case "AMZN":
                this.companyName = "Amazon";
                break;
            default:
                this.companyName = "Facebook";
                break;
        }
        this.currentPrice = barData.get(barData.size() - 1).getClose();
        this.open = barData.get(0).getOpen();
        this.volume = quoteData.getTotalVolume();
        // TODO: fix Optional.get
        this.low = barData.stream().min(Comparator.comparing(BarData::getLow)).get().getLow();
        this.high = barData.stream().max(Comparator.comparing(BarData::getHigh)).get().getHigh();
        this.yearlyLow = quoteData.getYearlyLow();
        this.yearlyHigh = quoteData.getYearlyHigh();
        this.peRatio = quoteData.getPeRatio();
        this.divYield = quoteData.getDivYield();
        this.description = "";
        this.graphData = barData;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Float getCurrentPrice() {
        return currentPrice;
    }

    public Float getOpen() {
        return open;
    }

    public Integer getVolume() {
        return volume;
    }

    public Float getLow() {
        return low;
    }

    public Float getHigh() {
        return high;
    }

    public Float getYearlyLow() {
        return yearlyLow;
    }

    public Float getYearlyHigh() {
        return yearlyHigh;
    }

    public Double getPeRatio() {
        return peRatio;
    }

    public Double getDivYield() {
        return divYield;
    }

    public String getDescription() {
        return description;
    }

    public List<BarData> getGraphData() {
        return graphData;
    }
}
