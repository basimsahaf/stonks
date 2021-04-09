package com.stonks.android.model;

public class CompanyModel {
    private String name;
    private String symbol;

    public CompanyModel(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getName() {
        return this.name;
    }
}
