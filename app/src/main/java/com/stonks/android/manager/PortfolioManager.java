package com.stonks.android.manager;

import android.content.Context;
import android.util.Log;

import com.stonks.android.HomePageFragment;
import com.stonks.android.MainActivity;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PortfolioManager {
    private static PortfolioManager portfolioManager = null;

    private HomePageFragment fragment;

    private static float profitFromTransactions = 0.0f;

    private static UserTable userTable;
    private static PortfolioTable portfolioTable;

    private static Portfolio portfolio;
    private static ArrayList<StockListItem> stocksList;
    private static ArrayList<Float> barData;
    private static ArrayList<Transaction> transactions;

    private PortfolioManager(Context context, HomePageFragment f) {
        fragment = f;
        userTable = new UserTable(context);
        portfolioTable = new PortfolioTable(context);
        stocksList = new ArrayList<>();
        barData = new ArrayList<>();
    }

    public static PortfolioManager getInstance(Context context, HomePageFragment f) {
        if (portfolioManager == null) {
            portfolioManager = new PortfolioManager(context, f);

            //  TODO: Fetch from the TransactionManager
            transactions = new ArrayList<>();
//            transactions.add(new Transaction("username", "SHOP", 1, 200.0f, TransactionMode.BUY, java.time.LocalDateTime.now().minusDays(3)));
//            transactions.add(new Transaction("username", "SHOP", 1, 2000.0f, TransactionMode.SELL, java.time.LocalDateTime.now().minusDays(2)));
            transactions.add(new Transaction("username", "SHOP", 2, 1000.0f, TransactionMode.BUY, java.time.LocalDateTime.now().minusDays(6)));
            transactions.add(new Transaction("username", "UBER", 2, 20.0f, TransactionMode.BUY, java.time.LocalDateTime.now().minusDays(6)));
            transactions.add(new Transaction("username", "SHOP", 1, 1200.0f, TransactionMode.SELL, java.time.LocalDateTime.now().minusDays(2)));
            transactions.add(new Transaction("username", "SHOP", 2, 1300.0f, TransactionMode.BUY, java.time.LocalDateTime.now().withHour(12)));
        }

        //String username = LoginRepository.getInstance(new LoginDataSource(userTable)).getCurrentUser();
        portfolio = new Portfolio(0.0f, 0.0f, portfolioTable.getPortfolioItems("username"), portfolioManager); // TODO: get user from table
        portfolioManager.fragment = f;

        portfolioManager.subscribePortfolioItems();

        return portfolioManager;
    }

    public void subscribePortfolioItems() {
        MainActivity activity = fragment.getMainActivity();
        for (PortfolioItem item : portfolio.getPortfolioItems()) {
            activity.subscribe(item.getSymbol(), item);

            item.addOnPropertyChangedCallback(
                    new androidx.databinding.Observable.OnPropertyChangedCallback() {
                        @Override
                        public void onPropertyChanged(
                                androidx.databinding.Observable observable, int i) {
                            updateData();
                        }
                    });
        }
    }

    public float getAccountBalance() {
        return portfolio.getAccountBalance();
    }

    public float getAccountValue() {
        return portfolio.getAccountValue();
    }

    public ArrayList<StockListItem> getStocks() {
        return stocksList;
    }

    public ArrayList<Float> getBarData() {
        return barData;
    }

    public void fetchInitialData() {
        ArrayList<String> symbolList = new ArrayList<>();
        for (PortfolioItem item : portfolio.getPortfolioItems()) {
            symbolList.add(item.getSymbol());
        }

        Symbols symbols = new Symbols(symbolList);

        MarketDataService marketDataService = new MarketDataService();
        marketDataService.getBars(symbols, AlpacaTimeframe.MINUTE, 390)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        map -> {
                            float accountValue = 0.0f;
                            stocksList.clear();
                            barData = new ArrayList<Float>(Collections.nCopies(390, 0.0f));

                            // TODO: Actually make bar data
                            for (BarData data : map.get("SHOP")) {
                                //barData.add(data.getClose());
                            }

                            for (int i = 0; i < symbolList.size(); i++) {
                                String symbol = symbolList.get(i);
                                if (portfolio.getStockQuantity(symbol) == 0) {
                                    continue;
                                }

                                ArrayList<Float> currStockData = createGraphData(symbol, map.get(symbol));

                                for (int j = 0; j < currStockData.size(); j++) {
                                    barData.set(j, barData.get(j) + currStockData.get(j));
                                }
                            }

                            // Calculates account value
                            for (String symbol : symbolList) {
                                List<BarData> barData = map.get(symbol);
                                float currentPrice = barData.get(barData.size() - 1).getClose();
                                float change = currentPrice - barData.get(0).getOpen();
                                float changePercentage = change * 100 / barData.get(0).getOpen();
                                int quantity = portfolio.getStockQuantity(symbol);

                                portfolio.setPrice(symbol, currentPrice);
                                stocksList.add(new StockListItem(symbol, currentPrice, quantity, change, changePercentage));

                                accountValue += (currentPrice * quantity);
                            }

                            calculateProfit();
                            portfolio.setAccountValue(accountValue);
                            fragment.updateData();
                        },
                        err -> Log.e("PortfolioManager", err.toString()));
    }

    // TODO: calculate for all transactions;
    public ArrayList<Float> createGraphData(String symbol, List<BarData> stockPrices) {
        ArrayList<Float> graphData = new ArrayList<>();

        while (graphData.size() < stockPrices.size()) {
            graphData.add(0.0f);
        }

        int totalQuantity = 0;
        float pricePerStock = 0.0f;
        for (Transaction transaction : transactions) {
            if (!transaction.getSymbol().equalsIgnoreCase(symbol)) {
                continue;
            }

            int quantity = transaction.getShares();

            if (transaction.getTransactionType() == TransactionMode.BUY) {
                if (totalQuantity == 0.0f) {
                    pricePerStock = 0.0f;
                }

                totalQuantity += transaction.getShares();
                pricePerStock = (pricePerStock + (transaction.getPrice() * transaction.getShares())) / totalQuantity;
            } else if (transaction.getTransactionType() == TransactionMode.SELL) {
                totalQuantity -= transaction.getShares();
                quantity *= -1;
            }

            LocalDateTime transactionDate = transaction.getCreatedAt();
            for (int i = 0; i < graphData.size(); i++) {
                Instant pointDate = new Date(stockPrices.get(i).getTimestamp() * 1000L).toInstant();
                LocalDateTime localPointDate = LocalDateTime.ofInstant(pointDate, ZoneId.systemDefault());

                if (localPointDate.isBefore(transactionDate)) {
                    float newValue;

                    if (transaction.getTransactionType() == TransactionMode.BUY) {
                        newValue = graphData.get(i) + (quantity * transaction.getPrice());
                    } else {
                        newValue = graphData.get(i) + (quantity * pricePerStock);
                    }

                    graphData.set(i, totalQuantity == 0.0f ? 0.0f : newValue);

                    continue;
                }

                float newValue = graphData.get(i) + (quantity * stockPrices.get(i).getClose());
                graphData.set(i, totalQuantity == 0.0f ? 0.0f : newValue);
            }
        }

        return graphData;
    }

    public float calculateProfit() {
        //  TODO: Fetch from the TransactionManager
        profitFromTransactions = 0.0f;

        for (Transaction transaction : transactions) {
            if (transaction.getTransactionType() == TransactionMode.BUY) {
                profitFromTransactions -= (transaction.getPrice() * transaction.getShares());
                continue;
            }

            profitFromTransactions += (transaction.getPrice() * transaction.getShares());
        }

        return profitFromTransactions;
    }

    public void updateData() {
        fragment.updateData();
    }

    public float graphChange() {
        float heldStocksReturn = barData.get(barData.size() - 1) - barData.get(0);

        return heldStocksReturn;
    }
}
