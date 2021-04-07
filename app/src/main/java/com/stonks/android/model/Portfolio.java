package com.stonks.android.model;

import com.stonks.android.manager.PortfolioManager;

import java.util.ArrayList;

public class Portfolio {
    float accountBalance;
    float accountValue;
    ArrayList<PortfolioItem> portfolioItems;
    PortfolioManager portfolioManager;


    public Portfolio(float accountBalance, float accountValue, ArrayList<PortfolioItem> portfolioItems, PortfolioManager portfolioManager) {
        this.accountBalance = accountBalance;
        this.accountValue = accountValue;
        this.portfolioItems = portfolioItems;
        this.portfolioManager = portfolioManager;

        // TODO: Remove
        if (portfolioItems.isEmpty()) {
            portfolioItems.add(new PortfolioItem("username", "SHOP", 3));
            //portfolioItems.add(new PortfolioItem("username", "UBER", 1));
        }
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
            if (portfolioItem.getSymbol().equalsIgnoreCase(symbol)) {
                return portfolioItem.getQuantity();
            }
        }

        return 0;
    }

    public void setPrice(String symbol, float price) {
        for (PortfolioItem portfolioItem : portfolioItems) {
            if (portfolioItem.getSymbol().equalsIgnoreCase(symbol)) {
                portfolioItem.updateCurrentPrice(price);
            }
        }
    }
}
