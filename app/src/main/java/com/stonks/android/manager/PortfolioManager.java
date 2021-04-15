package com.stonks.android.manager;

import android.content.Context;
import android.util.Log;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.stonks.android.HomePageFragment;
import com.stonks.android.MainActivity;
import com.stonks.android.external.MarketDataService;
import com.stonks.android.model.BarData;
import com.stonks.android.model.LoginRepository;
import com.stonks.android.model.Portfolio;
import com.stonks.android.model.PortfolioItem;
import com.stonks.android.model.StockListItem;
import com.stonks.android.model.Symbols;
import com.stonks.android.model.Transaction;
import com.stonks.android.model.TransactionMode;
import com.stonks.android.model.alpaca.AlpacaTimeframe;
import com.stonks.android.model.alpaca.DateRange;
import com.stonks.android.storage.PortfolioTable;
import com.stonks.android.storage.TransactionTable;
import com.stonks.android.storage.UserTable;
import com.stonks.android.uicomponent.StockChart;
import com.stonks.android.utility.ChartHelpers;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PortfolioManager {
    private static PortfolioManager portfolioManager = null;

    final int limit = 1000;
    private float allTimeChange;
    private DateRange currentRange;
    private HomePageFragment fragment;

    private final UserTable userTable;
    private final PortfolioTable portfolioTable;
    private final TransactionTable transactionTable;

    private static boolean isUpdating = false;
    private static String username;
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
    }

    public static PortfolioManager getInstance(Context context, HomePageFragment f) {
        if (portfolioManager == null) {
            portfolioManager = new PortfolioManager(context, f);

            graphData = new ArrayList<>();
            stocksList = new ArrayList<>();
        }

        username = LoginRepository.getInstance(context).getCurrentUser();
        transactions = portfolioManager.transactionTable.getTransactions(username);
        symbolList = portfolioManager.portfolioTable.getSymbols(username);

        portfolio =
                new Portfolio(
                        portfolioManager.userTable.getFunds(username),
                        0.0f,
                        portfolioManager.portfolioTable.getPortfolioItems(username),
                        portfolioManager);
        portfolioManager.fragment = f;
        portfolioManager.currentRange = DateRange.DAY;

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

    public void unsubscribePortfolioItems() {
        MainActivity activity = fragment.getMainActivity();
        for (PortfolioItem item : portfolio.getPortfolioItems()) {
            activity.unsubscribe(item.getSymbol());
        }
    }

    public float getAccountBalance() {
        return portfolio.getAccountBalance();
    }

    public float getAccountValue() {
        return portfolio.getAccountValue();
    }

    public LocalDateTime getStartDate() {
        return userTable.getTrainingStartDate(username);
    }

    public ArrayList<StockListItem> getStocksList() {
        return stocksList;
    }

    public ArrayList<Float> getGraphData() {
        return graphData;
    }

    public float getGraphChange() {
        return graphData.isEmpty() ? 0 : graphData.get(graphData.size() - 1) - graphData.get(0);
    }

    public LineDataSet getStockChartData() {
        return stockChartData;
    }

    public float getTotalReturn() {
        return allTimeChange;
    }

    public void setCurrentRange(DateRange range) {
        if (this.currentRange == range) {
            return;
        }

        this.currentRange = range;
        calculateData(true);
    }

    public ArrayList<Float> createGraphData(
            String symbol, List<BarData> stockPrices, boolean isCalculatingTotalReturn) {
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
                LocalDateTime localPointDate =
                        ChartHelpers.convertEpochToDateTime(stockPrices.get(i).getTimestamp());

                boolean isBeforeTransactionDate;
                if (currentRange == DateRange.YEAR
                        || currentRange == DateRange.THREE_YEARS
                        || isCalculatingTotalReturn) {
                    isBeforeTransactionDate =
                            localPointDate.toLocalDate().isBefore(transactionDate.toLocalDate());
                } else {
                    isBeforeTransactionDate = localPointDate.isBefore(transactionDate);
                }

                if (isBeforeTransactionDate) {
                    float newValue;
                    if (transaction.getTransactionType() == TransactionMode.BUY) {
                        newValue = stockGraphData.get(i) + (quantity * transaction.getPrice());
                    } else {
                        newValue = stockGraphData.get(i) + (quantity * pricePerStock);
                    }

                    stockGraphData.set(i, totalQuantity == 0 ? 0.0f : newValue);

                    continue;
                }

                float newValue = stockGraphData.get(i) + (quantity * stockPrices.get(i).getClose());
                stockGraphData.set(i, totalQuantity == 0 ? 0.0f : newValue);
            }
        }

        return stockGraphData;
    }

    public void calculateAccountValue(Map<String, List<BarData>> stocksData) {
        float accountValue = 0.0f;
        stocksList = new ArrayList<>();

        for (String symbol : symbolList) {
            List<BarData> priceList = stocksData.get(symbol);
            float currentPrice = priceList.get(priceList.size() - 1).getClose();
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

    public void calculateTotalReturn(boolean graphOnly) {
        if (currentRange == DateRange.THREE_YEARS) {
            allTimeChange = getGraphChange();

            if (graphOnly) {
                fragment.updateGraphData();
            } else {
                fragment.updateData();
            }

            isUpdating = false;

            return;
        }

        MarketDataService marketDataService = new MarketDataService();
        marketDataService
                .getBars(new Symbols(symbolList), AlpacaTimeframe.DAY, limit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        map -> {
                            allTimeChange = 0.0f;

                            for (int i = 0; i < symbolList.size(); i++) {
                                String symbol = symbolList.get(i);
                                if (portfolio.getStockQuantity(symbol) == 0) {
                                    continue;
                                }

                                List<BarData> symbolData = map.get(symbol);
                                long firstTimeStamp =
                                        ChartHelpers.getEpochTimestamp(
                                                DateRange.THREE_YEARS,
                                                symbolData
                                                        .get(symbolData.size() - 1)
                                                        .getTimestamp());
                                symbolData =
                                        symbolData.stream()
                                                .filter(bar -> bar.getTimestamp() >= firstTimeStamp)
                                                .collect(Collectors.toList());

                                List<BarData> barData =
                                        ChartHelpers.cleanData(symbolData, AlpacaTimeframe.DAY);
                                List<BarData> clubbedBars = ChartHelpers.mergeBars(barData, 1);
                                List<Float> stockData = createGraphData(symbol, clubbedBars, true);

                                float change =
                                        stockData.get(stockData.size() - 1) - stockData.get(0);
                                allTimeChange += change;
                            }

                            if (graphOnly) {
                                fragment.updateGraphData();
                            } else {
                                fragment.updateData();
                            }

                            isUpdating = false;
                        },
                        err -> {
                            isUpdating = false;
                            Log.e("PortfolioManager", err.toString());
                        });
    }

    public void calculateData(boolean graphOnly) {
        if (isUpdating) {
            return;
        }

        if (symbolList.isEmpty()) {
            fragment.updateData();
            return;
        }

        isUpdating = true;

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
        final int finalWindowSize = windowSize;

        MarketDataService marketDataService = new MarketDataService();
        marketDataService
                .getBars(symbols, timeframe, limit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        map -> {
                            graphData = new ArrayList<>();
                            List<BarData> biggestList = new ArrayList<>();
                            Map<String, List<BarData>> stockData = new HashMap<>();

                            // Calculate all clubbed bars first
                            for (String symbol : symbolList) {
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

                                List<BarData> barData =
                                        ChartHelpers.cleanData(symbolData, timeframe);
                                List<BarData> clubbedBars =
                                        ChartHelpers.mergeBars(barData, finalWindowSize);
                                stockData.put(symbol, clubbedBars);

                                if (clubbedBars.size() > biggestList.size()) {
                                    biggestList = clubbedBars;
                                }
                            }

                            while (graphData.size() < biggestList.size()) {
                                graphData.add(0.0f);
                            }

                            for (String symbol : symbolList) {
                                ArrayList<Float> currStockData =
                                        createGraphData(symbol, stockData.get(symbol), false);

                                int currStockIndex = 0;
                                for (int j = 0; j < graphData.size(); j++) {
                                    LocalDateTime graphDate =
                                            ChartHelpers.convertEpochToDateTime(
                                                    biggestList.get(j).getTimestamp());
                                    LocalDateTime currStockIndexDate =
                                            ChartHelpers.convertEpochToDateTime(
                                                    stockData
                                                            .get(symbol)
                                                            .get(currStockIndex)
                                                            .getTimestamp());
                                    if (graphDate.isAfter(currStockIndexDate)
                                            && currStockIndex < currStockData.size() - 1) {
                                        currStockIndex++;
                                    }

                                    graphData.set(
                                            j,
                                            graphData.get(j) + currStockData.get(currStockIndex));
                                }
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
                            calculateTotalReturn(graphOnly);
                        },
                        err -> {
                            isUpdating = false;
                            Log.e("PortfolioManager", err.toString());
                        });
    }
}
