package com.stonks.android.manager;

import android.content.Context;
import android.util.Log;

import com.stonks.android.HomePageFragment;
import com.stonks.android.external.MarketDataService;
import com.stonks.android.model.BarData;
import com.stonks.android.model.Portfolio;
import com.stonks.android.model.PortfolioItem;
import com.stonks.android.model.StockListItem;
import com.stonks.android.model.Symbols;
import com.stonks.android.model.Transaction;
import com.stonks.android.model.TransactionMode;
import com.stonks.android.model.alpaca.AlpacaTimeframe;
import com.stonks.android.storage.PortfolioTable;
import com.stonks.android.storage.UserTable;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PortfolioManager {
    private static PortfolioManager portfolioManager = null;

    private static HomePageFragment fragment;
    private static UserTable userTable;
    private static PortfolioTable portfolioTable;
    private static Portfolio portfolio;
    private static MarketDataService marketDataService;
    private static ArrayList<StockListItem> stocksList;

    private PortfolioManager(Context context, HomePageFragment f) {
        fragment = f;
        userTable = new UserTable(context);
        portfolioTable = new PortfolioTable(context);
        marketDataService = new MarketDataService();
        stocksList = new ArrayList<>();
    }

    public static PortfolioManager getInstance(Context context, HomePageFragment f) {
        if (portfolioManager == null) {
            portfolioManager = new PortfolioManager(context, f);
        }

        //String username = LoginRepository.getInstance(new LoginDataSource(userTable)).getCurrentUser();
        portfolio = new Portfolio(0.0f, 0.0f, portfolioTable.getPortfolioItems("username"));

        ArrayList<String> symbolList = new ArrayList<>();
        for (PortfolioItem item : portfolio.getPortfolioItems()) {
            symbolList.add(item.getSymbol());
        }
        symbolList.add("SHOP");
        symbolList.add("UBER");

        Symbols symbols = new Symbols(symbolList);
        marketDataService.getBars(symbols, AlpacaTimeframe.MINUTE, 10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        map -> {
                            float accountValue = 0.0f;
                            stocksList.clear();

                            for (String symbol : symbolList) {
                                List<BarData> barData = map.get(symbol);
                                float currentPrice = barData.get(barData.size() - 1).getClose();
                                float change = currentPrice - barData.get(0).getOpen();
                                float changePercentage = change * 100 / barData.get(0).getOpen();
                                int quantity = portfolio.getStockQuantity(symbol);

                                stocksList.add(new StockListItem(symbol, currentPrice, quantity, change, changePercentage));

                                accountValue += (currentPrice * quantity);
                                portfolio.setAccountValue(accountValue);
                            }

                            fragment.updateData();
                        },
                        err -> Log.e("PortfolioManager", err.toString()));

        return portfolioManager;
    }

    public float getAccountBalance() {
        return portfolio.getAccountBalance();
    }

    public float getAccountValue() {
        return portfolio.getAccountValue();
    }

    public float calculateProfit() {
        //  TODO: Fetch from the TransactionManager
        float profit = 0.0f;

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("username", "SHOP", 2, 10.0f, TransactionMode.BUY, java.time.LocalDateTime.now()));
        transactions.add(new Transaction("username", "SHOP", 1, 12.0f, TransactionMode.SELL, java.time.LocalDateTime.now()));
        transactions.add(new Transaction("username", "SHOP", 2, 13.0f, TransactionMode.BUY, java.time.LocalDateTime.now()));
        transactions.add(new Transaction("username", "SHOP", 3, 50.0f, TransactionMode.SELL, java.time.LocalDateTime.now()));

        for (Transaction transaction : transactions) {
            if (transaction.getTransactionType() == TransactionMode.BUY) {
                profit -= (transaction.getPrice() * transaction.getShares());
                continue;
            }

            profit += (transaction.getPrice() * transaction.getShares());
        }

        return profit;
    }

    public ArrayList<StockListItem> getStocks() {
        ArrayList<StockListItem> list = new ArrayList<>();

        for (PortfolioItem p : portfolio.getPortfolioItems()) {
            list.add(new StockListItem(p.getSymbol(), 100.0f, p.getQuantity(), 7.0f, 3.4f));
        }

        if (list.isEmpty()) {
            list.add(new StockListItem("SHOP", 100.0f, 2, 7.0f, 7.0f));
            list.add(new StockListItem("UBER", 100.0f, 2, 7.0f, 7.0f));
        }

        return stocksList;
    }
}
