package com.stonks.android.manager;

import android.content.Context;

import com.stonks.android.model.Portfolio;
import com.stonks.android.model.PortfolioItem;
import com.stonks.android.model.StockListItem;
import com.stonks.android.storage.PortfolioTable;
import com.stonks.android.storage.UserTable;

import java.util.ArrayList;

public class PortfolioManager {
    private static PortfolioManager portfolioManager = null;

    private static UserTable userTable;
    private static PortfolioTable portfolioTable;
    private static Portfolio portfolio;

    private PortfolioManager(Context context) {
        userTable = new UserTable(context);
        portfolioTable = new PortfolioTable(context);
    }

    public static PortfolioManager getInstance(Context context) {
        if (portfolioManager == null) {
            portfolioManager = new PortfolioManager(context);

            //String username = LoginRepository.getInstance(new LoginDataSource(userTable)).getCurrentUser();
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

    public ArrayList<StockListItem> getStocks() {
        ArrayList<StockListItem> list = new ArrayList<>();

        for (PortfolioItem p : portfolio.getPortfolioItems()) {
            list.add(new StockListItem(p.getSymbol(), 100.0f, p.getQuantity(), 7.0f, 3.4f));
        }

        if (list.isEmpty()) {
            list.add(new StockListItem("SHOP", 100.0f, 2, 7.0f, 7.0f));
        }

        return list;
    }
}
