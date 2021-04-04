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

    public void setAccountBalance(float accountBalance) {
        this.accountBalance = accountBalance;
    }

    public float getAccountValue() {
        return accountValue;
    }

    public ArrayList<PortfolioItem> getPortfolioItems() {
        return portfolioItems;
    }
}
