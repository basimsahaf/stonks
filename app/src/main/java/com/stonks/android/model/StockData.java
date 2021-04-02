package com.stonks.android.model;

import android.util.Log;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingConversion;
import com.stonks.android.BR;
import com.stonks.android.utility.Formatters;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public class StockData extends BaseObservable implements Serializable, WebSocketObserver {
    private String symbol;
    private String companyName;
    private Float currentPrice;
    private Float open;
    private Integer volume;
    private Float low;
    private Float high;
    private Float yearlyLow;
    private Float yearlyHigh;
    private Double peRatio;
    private Double divYield;
    private String description;
    private List<BarData> graphData;

    public StockData() {}

    public StockData(List<BarData> barData, QuoteData quoteData) {
        this.symbol = quoteData.getSymbol();
        // TODO: define this.companyName
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
        Log.d("volume", this.volume + "");
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

    @Bindable
    public String getSymbol() {
        return symbol;
    }

    @Bindable
    public String getCompanyName() {
        return companyName;
    }

    @Bindable
    public Float getCurrentPrice() {
        return currentPrice;
    }

    @Bindable
    public Float getOpen() {
        return open;
    }

    @Bindable
    public Integer getVolume() {
        return volume;
    }

    @Bindable
    public Float getLow() {
        return low;
    }

    @Bindable
    public Float getHigh() {
        return high;
    }

    @Bindable
    public Float getYearlyLow() {
        return yearlyLow;
    }

    @Bindable
    public Float getYearlyHigh() {
        return yearlyHigh;
    }

    public Double getPeRatio() {
        return peRatio;
    }

    public Double getDivYield() {
        return divYield;
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    public List<BarData> getGraphData() {
        return graphData;
    }

    @Override
    public void updateCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
        notifyPropertyChanged(BR.currentPrice);
    }

    public void updateStock(StockData newData) {
        symbol = newData.symbol;
        companyName = newData.companyName;
        currentPrice = newData.currentPrice;
        open = newData.open;
        volume = newData.volume;
        low = newData.low;
        high = newData.high;
        yearlyLow = newData.yearlyLow;
        yearlyHigh = newData.yearlyHigh;
        peRatio = newData.peRatio;
        divYield = newData.divYield;
        description = newData.description;
        graphData = newData.graphData;

        notifyChange();
    }

    @BindingConversion
    public static String convertFloatToString(Float val) {
        if (val == null) {
            return "";
        }

        return Formatters.formatPrice(val);
    }
}
