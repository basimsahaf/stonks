package com.stonks.android.model;

import java.util.ArrayList;

public class Portfolio {
    float accountBalance;
    float accountValue;
    ArrayList<PortfolioItem> portfolioItems;

    public Portfolio(float accountBalance, float accountValue, ArrayList<PortfolioItem> portfolioItems) {
        this.accountBalance = accountBalance;
        this.accountValue = accountValue;
        this.portfolioItems = portfolioItems;
    }

    public float getAccountBalance() {
        return accountBalance;
    }

    public float getAccountValue() {
        return accountValue;
    }

    public void setAccountValue(float accountValue) {
        this.accountValue = accountValue;
    }

    public ArrayList<PortfolioItem> getPortfolioItems() {
        return portfolioItems;
    }

    public int getStockQuantity(String symbol) {
        for (PortfolioItem portfolioItem : portfolioItems) {
            if (portfolioItem.getSymbol() == symbol) {
                return portfolioItem.getQuantity();
            }
        }

        return 10;
    }
}
