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
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PortfolioManager {
    private static PortfolioManager portfolioManager = null;

    private static HomePageFragment fragment;

    private static UserTable userTable;
    private static PortfolioTable portfolioTable;

    private static Portfolio portfolio;
    private static ArrayList<StockListItem> stocksList;
    private static ArrayList<Float> barData;

    static TemporalAdjuster YESTERDAY =
            TemporalAdjusters.ofDateAdjuster(date -> date.minusDays(1));
    static TemporalAdjuster LAST_WEEK =
            TemporalAdjusters.ofDateAdjuster(date -> date.minusWeeks(1));
    static TemporalAdjuster LAST_MONTH =
            TemporalAdjusters.ofDateAdjuster(date -> date.minusMonths(1));
    static TemporalAdjuster LAST_YEAR =
            TemporalAdjusters.ofDateAdjuster(date -> date.minusYears(1));
    static TemporalAdjuster THREE_YEARS_AGO =
            TemporalAdjusters.ofDateAdjuster(date -> date.minusYears(3));

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
        }

        //String username = LoginRepository.getInstance(new LoginDataSource(userTable)).getCurrentUser();
        portfolio = new Portfolio(0.0f, 0.0f, portfolioTable.getPortfolioItems("username"), portfolioManager); // TODO: get user from table
        fragment = f;

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
                            barData.clear();

                            // TODO: Actually make bar data
                            for (BarData data : map.get("SHOP")) {
                                barData.add(data.getClose());
                            }

                            createGraphData(map.get("SHOP"));

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

                            portfolio.setAccountValue(accountValue);
                            fragment.updateData();
                        },
                        err -> Log.e("PortfolioManager", err.toString()));
    }

    // TODO: calculate for all transactions;
    public void createGraphData(List<BarData> stockPrices) {
        ArrayList<Float> graphData = new ArrayList<>();

        //  TODO: Fetch from the TransactionManager
        float profit = 0.0f;

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("username", "SHOP", 2, 1000.0f, TransactionMode.BUY, java.time.LocalDateTime.now().withHour(12)));
        transactions.add(new Transaction("username", "SHOP", 1, 1200.0f, TransactionMode.SELL, java.time.LocalDateTime.now().withHour(15)));
        transactions.add(new Transaction("username", "SHOP", 2, 1300.0f, TransactionMode.BUY, java.time.LocalDateTime.now().withHour(16)));

        while (graphData.size() < stockPrices.size()) {
            graphData.add(0.0f);
        }

        int totalQuantity = 0;
        float pricePerStock = 0.0f;
        for (Transaction transaction : transactions) {
            int quantity = transaction.getShares();
            totalQuantity += transaction.getShares();

            if (transaction.getTransactionType() == TransactionMode.BUY) {
                pricePerStock = (pricePerStock + (transaction.getPrice() * transaction.getShares())) / totalQuantity;
            } else if (transaction.getTransactionType() == TransactionMode.SELL) {
                quantity *= -1;
            }

            LocalDateTime transactionDate = transaction.getCreatedAt();
            for (int i = 0; i < graphData.size(); i++) {
                Instant pointDate = new Date(stockPrices.get(i).getTimestamp() * 1000L).toInstant();
                LocalDateTime localPointDate = LocalDateTime.ofInstant(pointDate, ZoneId.systemDefault());

                if (transactionDate.isBefore(localPointDate)) {
                    float newValue;

                    if (transaction.getTransactionType() == TransactionMode.BUY) {
                        newValue = graphData.get(i) + (quantity * transaction.getPrice());
                    } else {
                        newValue = graphData.get(i) + (quantity * pricePerStock);
                    }

                    graphData.set(i, newValue);

                    continue;
                }

                float newValue = graphData.get(i) + (quantity * stockPrices.get(i).getClose());
                graphData.set(i, newValue);
            }
        }

    }

    public float calculateProfit() {
        //  TODO: Fetch from the TransactionManager
        float profit = 0.0f;

        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("username", "SHOP", 2, 10.0f, TransactionMode.BUY, java.time.LocalDateTime.now()));
        transactions.add(new Transaction("username", "SHOP", 1, 12.0f, TransactionMode.SELL, java.time.LocalDateTime.now()));
        transactions.add(new Transaction("username", "SHOP", 2, 13.0f, TransactionMode.BUY, java.time.LocalDateTime.now()));
        transactions.add(new Transaction("username", "SHOP", 3, 50.0f, TransactionMode.SELL, java.time.LocalDateTime.now()));

        Calendar calendar = Calendar.getInstance();
        LocalDateTime now = java.time.LocalDateTime.now();
        LocalDateTime yesterday = now.with(YESTERDAY);
        LocalDateTime lastWeek = now.with(LAST_WEEK);
        LocalDateTime lastMonth = now.with(LAST_MONTH);
        LocalDateTime lastYear = now.with(LAST_YEAR);
        LocalDateTime threeYearsAgo = now.with(THREE_YEARS_AGO);

        for (Transaction transaction : transactions) {
            if (transaction.getTransactionType() == TransactionMode.BUY) {
                profit -= (transaction.getPrice() * transaction.getShares());
                continue;
            }

            profit += (transaction.getPrice() * transaction.getShares());
        }

        return profit;
    }

    public void updateData() {
        portfolio.setAccountValue(9999.0f);
        fragment.updateData();
    }
}
