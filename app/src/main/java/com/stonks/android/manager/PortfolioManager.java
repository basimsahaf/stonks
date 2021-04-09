package com.stonks.android.manager;

import android.content.Context;
import android.util.Log;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.stonks.android.HomePageFragment;
import com.stonks.android.MainActivity;
import com.stonks.android.external.MarketDataService;
import com.stonks.android.model.*;
import com.stonks.android.model.alpaca.AlpacaTimeframe;
import com.stonks.android.model.alpaca.DateRange;
import com.stonks.android.storage.PortfolioTable;
import com.stonks.android.storage.TransactionTable;
import com.stonks.android.storage.UserTable;
import com.stonks.android.uicomponent.StockChart;
import com.stonks.android.utility.ChartHelpers;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PortfolioManager {
    private static PortfolioManager portfolioManager = null;

    final int limit = 1000;
    private DateRange currentRange;
    private HomePageFragment fragment;

    private final UserTable userTable;
    private final PortfolioTable portfolioTable;
    private final TransactionTable transactionTable;
    private final LoginRepository loginRepository;

    private static Portfolio portfolio;
    private static LineDataSet stockChartData;
    private static ArrayList<Float> graphData;
    private static ArrayList<StockListItem> stocksList;
    private static ArrayList<Transaction> transactions;
    private static ArrayList<String> symbolList;

    private PortfolioManager(Context context, HomePageFragment f) {
        fragment = f;
        userTable = UserTable.getInstance(context);
        portfolioTable = PortfolioTable.getInstance(context);
        transactionTable = TransactionTable.getInstance(context);
        loginRepository = LoginRepository.getInstance(context);

        transactionTable.addTransaction(
                new Transaction(
                        loginRepository.getCurrentUser(),
                        "GME",
                        10,
                        10f,
                        TransactionMode.BUY,
                        LocalDateTime.now().minusMonths(2)));
        portfolioTable.addPortfolioItem(
                new PortfolioItem(loginRepository.getCurrentUser(), "GME", 10));
    }

    public static PortfolioManager getInstance(Context context, HomePageFragment f) {
        if (portfolioManager == null) {
            portfolioManager = new PortfolioManager(context, f);

            //  TODO: Remove after testing
            //            if (transactions.isEmpty()) {
            //                //                transactions.add(new Transaction("username", "SHOP",
            // 1, 200.0f,
            //                // TransactionMode.BUY, java.time.LocalDateTime.now().minusDays(3)));
            //                //                transactions.add(new Transaction("username", "SHOP",
            // 1, 2000.0f,
            //                // TransactionMode.SELL, java.time.LocalDateTime.now().minusDays(2)));
            //                transactions.add(
            //                        new Transaction(
            //                                username,
            //                                "SHOP",
            //                                1,
            //                                1000.0f,
            //                                TransactionMode.BUY,
            //                                java.time.LocalDateTime.now().withHour(10)));
            //                transactions.add(
            //                        new Transaction(
            //                                username,
            //                                "SHOP",
            //                                2,
            //                                1300.0f,
            //                                TransactionMode.BUY,
            //                                java.time.LocalDateTime.now().withHour(12)));
            //                //                transactions.add(new Transaction("username", "UBER",
            // 2, 20.0f,
            //                // TransactionMode.BUY, java.time.LocalDateTime.now().withHour(14)));
            //
            //                symbolList.add("SHOP");
            //                //                symbolList.add("UBER");
            //            }
        }

        String username = portfolioManager.loginRepository.getCurrentUser();
        portfolio =
                new Portfolio(
                        portfolioManager.userTable.getTotalAmountAvailable(username),
                        0.0f,
                        portfolioManager.portfolioTable.getPortfolioItems(
                                LoginRepository.getInstance(context).getCurrentUser()),
                        portfolioManager);
        portfolioManager.fragment = f;
        portfolioManager.currentRange = DateRange.DAY;

        transactions = new ArrayList<>();
        transactions = portfolioManager.transactionTable.getTransactions(username);
        symbolList = portfolioManager.transactionTable.getSymbols(username);

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
                            calculateData(false);
                        }
                    });
        }
    }

    public float getAccountBalance() {
        return portfolio.getAccountBalance();
    }

    public String getTrainingStartDate() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy");
        LocalDateTime dateTime = userTable.getTrainingStartDate(loginRepository.getCurrentUser());

        return dateTime.format(formatter);
    }

    public float getAccountValue() {
        return portfolio.getAccountValue();
    }

    public ArrayList<StockListItem> getStocksList() {
        return stocksList;
    }

    public void setCurrentRange(DateRange range) {
        this.currentRange = range;
        calculateData(true);
    }

    public ArrayList<Float> createGraphData(String symbol, List<BarData> stockPrices) {
        ArrayList<Float> stockGraphData = new ArrayList<>();

        while (stockGraphData.size() < stockPrices.size()) {
            stockGraphData.add(0.0f);
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
                pricePerStock =
                        (pricePerStock + (transaction.getPrice() * transaction.getShares()))
                                / totalQuantity;
            } else if (transaction.getTransactionType() == TransactionMode.SELL) {
                totalQuantity -= transaction.getShares();
                quantity *= -1;
            }

            LocalDateTime transactionDate = transaction.getCreatedAt();
            for (int i = 0; i < stockGraphData.size(); i++) {
                Instant pointDate = new Date(stockPrices.get(i).getTimestamp() * 1000L).toInstant();
                LocalDateTime localPointDate =
                        LocalDateTime.ofInstant(pointDate, ZoneId.systemDefault());

                if (localPointDate.isBefore(transactionDate)) {
                    float newValue;

                    if (transaction.getTransactionType() == TransactionMode.BUY) {
                        newValue = stockGraphData.get(i) + (quantity * transaction.getPrice());
                    } else {
                        newValue = stockGraphData.get(i) + (quantity * pricePerStock);
                    }

                    stockGraphData.set(i, totalQuantity == 0.0f ? 0.0f : newValue);

                    continue;
                }

                float newValue = stockGraphData.get(i) + (quantity * stockPrices.get(i).getOpen());
                stockGraphData.set(i, totalQuantity == 0.0f ? 0.0f : newValue);
            }
        }

        return stockGraphData;
    }

    public ArrayList<Float> getGraphData() {
        return graphData;
    }

    public LineDataSet getStockChartData() {
        return stockChartData;
    }

    public float getTransactionProfits() {
        float transactionProfits = 0.0f;

        for (Transaction transaction : transactions) {
            if (transaction.getTransactionType() == TransactionMode.BUY) {
                transactionProfits -= (transaction.getPrice() * transaction.getShares());
                continue;
            }

            transactionProfits += (transaction.getPrice() * transaction.getShares());
        }

        return transactionProfits;
    }

    public float getGraphChange() {
        return graphData.get(graphData.size() - 1) - graphData.get(0);
    }

    public void calculateAccountValue(Map<String, List<BarData>> stocksData) {
        float accountValue = 0.0f;
        stocksList = new ArrayList<>();

        for (String symbol : symbolList) {
            if (portfolio.getStockQuantity(symbol) == 0) {
                continue;
            }

            List<BarData> priceList = stocksData.get(symbol);
            float currentPrice = priceList.get(priceList.size() - 1).getOpen();
            float change = currentPrice - priceList.get(0).getOpen();
            float changePercentage = change * 100 / priceList.get(0).getOpen();
            int quantity = portfolio.getStockQuantity(symbol);

            portfolio.setPrice(symbol, currentPrice);
            stocksList.add(
                    new StockListItem(
                            symbol, "", currentPrice, quantity, change, changePercentage));

            accountValue += (currentPrice * quantity);
        }

        portfolio.setAccountValue(accountValue);
    }

    public void calculateData(boolean graphOnly) {
        //        ArrayList<String> symbolList = new ArrayList<>();
        //        for (PortfolioItem item : portfolio.getPortfolioItems()) {
        //            symbolList.add(item.getSymbol());
        //        }

        // Information for graph data
        Symbols symbols = new Symbols(symbolList);
        AlpacaTimeframe timeframe = ChartHelpers.getDataPointTimeframe(this.currentRange);
        int windowSize = 1;
        switch (currentRange) {
            case DAY:
                windowSize = 5;
                break;
            case MONTH:
                windowSize = 4;
                break;
        }

        Log.d(
                "Portfolio",
                "making a request with: "
                        + symbols.toString()
                        + ", "
                        + timeframe.toString()
                        + ", "
                        + limit);

        MarketDataService marketDataService = new MarketDataService();
        int finalWindowSize = windowSize;
        marketDataService
                .getBars(symbols, timeframe, limit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        map -> {
                            graphData = new ArrayList<>();
                            Map<String, List<BarData>> stockData = new HashMap<>();

                            for (int i = 0; i < symbolList.size(); i++) {
                                Log.d("symbol loop", "start");
                                String symbol = symbolList.get(i);
                                if (portfolio.getStockQuantity(symbol) == 0) {
                                    continue;
                                }

                                List<BarData> symbolData = map.get(symbol);
                                long firstTimeStamp =
                                        ChartHelpers.getEpochTimestamp(
                                                currentRange,
                                                symbolData
                                                        .get(symbolData.size() - 1)
                                                        .getTimestamp());
                                symbolData =
                                        symbolData.stream()
                                                .filter(bar -> bar.getTimestamp() >= firstTimeStamp)
                                                .collect(Collectors.toList());

                                Log.d("cleaning", "start");
                                List<BarData> barData =
                                        ChartHelpers.cleanData(symbolData, timeframe);
                                Log.d("cleaning", "start");

                                Log.d("merging", "start");
                                List<BarData> clubbedBars =
                                        ChartHelpers.mergeBars(barData, finalWindowSize);

                                Log.d("merging", "start");
                                stockData.put(symbol, clubbedBars);

                                ArrayList<Float> currStockData =
                                        createGraphData(symbol, clubbedBars);

                                while (graphData.size() < currStockData.size()) {
                                    graphData.add(0.0f);
                                }

                                Log.d("final for loop", "start");

                                for (int j = 0; j < currStockData.size(); j++) {
                                    graphData.set(j, graphData.get(j) + currStockData.get(j));
                                }
                                Log.d("final for loop", "end");
                            }

                            AtomicInteger x = new AtomicInteger(1);
                            List<Entry> lineData =
                                    graphData.stream()
                                            .map(
                                                    floatVal ->
                                                            new Entry(
                                                                    x.getAndIncrement(), floatVal))
                                            .collect(Collectors.toList());
                            stockChartData = StockChart.buildDataSet(lineData);

                            calculateAccountValue(stockData);

                            if (graphOnly) {
                                fragment.updateGraph();
                            } else {
                                fragment.updateData();
                            }
                        },
                        err -> Log.e("PortfolioManager", err.toString()));
    }
}
