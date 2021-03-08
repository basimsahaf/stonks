package com.stonks.android.model;

public class SearchResult {
    private String companyName;
    private String symbol;

    public SearchResult(String companyName, String symbol) {
        this.companyName = companyName;
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getSymbol() {
        return symbol;
    }
}
