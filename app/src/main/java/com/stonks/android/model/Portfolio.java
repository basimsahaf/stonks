package com.stonks.android.model;

import java.util.ArrayList;

public class Portfolio {
    private final ArrayList<PortfolioRow> portfolioRows;

    public Portfolio(ArrayList<PortfolioRow> portfolioRows) {
        this.portfolioRows = portfolioRows;
    }

    public ArrayList<PortfolioRow> getPortfolioRows() {
        return portfolioRows;
    }
}
