package com.stonks.android.manager;

import android.content.Context;

import com.stonks.android.model.Portfolio;
import com.stonks.android.model.PortfolioItem;
import com.stonks.android.storage.PortfolioTable;

import java.util.ArrayList;

public class PortfolioManager {
    private static PortfolioManager portfolioManager = null;

    private static PortfolioTable portfolioTable;
    private static Portfolio portfolio;

    private PortfolioManager(Context context) {
        portfolioTable = new PortfolioTable(context);
    }

    public static PortfolioManager getInstance(Context context) {
        if (portfolioManager == null) {
            portfolioManager = new PortfolioManager(context);

            portfolio = new Portfolio(0.0f, 0.0f, portfolioTable.getPortfolioItems("username"));
        }

        return portfolioManager;
    }

    public float getAccountBalance() {
        return portfolio.getAccountBalance();
    }

    public float getAccountValue() {
        return portfolio.getAccountValue();
    }

    public ArrayList<PortfolioItem> getStocks() {
        return portfolio.getPortfolioItems();
    }
}
