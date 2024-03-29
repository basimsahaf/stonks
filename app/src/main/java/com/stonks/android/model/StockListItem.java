package com.stonks.android.model;

public class StockListItem {
    private String stockSymbol;
    private String companyName;
    private float price;
    private int quantity;
    private float priceChange;
    private float changePercent;

    public StockListItem(
            String stockSymbol,
            String companyName,
            float price,
            int quantity,
            float priceChange,
            float changePercent) {
        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
        this.price = price;
        this.quantity = quantity;
        this.priceChange = priceChange;
        this.changePercent = changePercent;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public float getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getPriceChange() {
        return priceChange;
    }

    public float getChangePercent() {
        return changePercent;
    }
}
