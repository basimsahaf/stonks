package com.stonks.android.manager;

import android.util.Log;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.stonks.android.external.MarketDataService;
import com.stonks.android.model.BarData;
import com.stonks.android.model.QuoteData;
import com.stonks.android.model.StockData;
import com.stonks.android.model.Symbols;
import com.stonks.android.model.alpaca.AlpacaTimeframe;
import com.stonks.android.model.alpaca.DateRange;
import com.stonks.android.utility.ChartHelpers;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StockManager {
    private static final String TAG = StockManager.class.getCanonicalName();
    private final String symbol;
    private final StockData stockData;
    private final MarketDataService marketDataService;
    private DateRange currentRange;
    private final HashMap<Integer, BarData> candleData;
    private final HashMap<Integer, BarData> lineData;
    private final Function<Integer, String> candleMarker;
    private final Function<Integer, String> lineMarker;

    public StockManager(String symbol) {
        this.symbol = symbol;
        this.stockData = new StockData();
        // TODO: use singleton
        this.marketDataService = new MarketDataService();
        this.currentRange = DateRange.DAY;
        this.candleData = new HashMap<>();
        this.lineData = new HashMap<>();
        this.candleMarker =
                (x) -> {
                    BarData bar = this.candleData.get(x);
                    if (bar == null) {
                        return "";
                    }

                    LocalDateTime start =
                            LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(bar.getTimestamp()),
                                    TimeZone.getDefault().toZoneId());
                    LocalDateTime end =
                            LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(bar.getEndTimestamp()),
                                    TimeZone.getDefault().toZoneId());
                    DateTimeFormatter formatter = ChartHelpers.getMarkerDateFormatter(this.currentRange);

                    return String.format("%s - %s", formatter.format(start), formatter.format(end));
                };

        this.lineMarker =
                (x) -> {
                    BarData bar = this.lineData.get(x);
                    if (bar == null) {
                        return "";
                    }

                    LocalDateTime date =
                            LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(bar.getTimestamp()),
                                    TimeZone.getDefault().toZoneId());

                    return ChartHelpers.getMarkerDateFormatter(this.currentRange).format(date);
                };
    }

    public Function<Integer, String> getCandleMarker() {
        return candleMarker;
    }

    public Function<Integer, String> getLineMarker() {
        return lineMarker;
    }

    public StockData getStockData() {
        return stockData;
    }

    public void setCurrentRange(DateRange newRange) {
        this.currentRange = newRange;
        this.fetchGraphData();
    }

    public void fetchInitialData() {
        final Symbols symbols = new Symbols(Collections.singletonList(this.symbol));
        final int limit = ChartHelpers.getDataPointLimit(this.currentRange);
        final AlpacaTimeframe timeframe = ChartHelpers.getDataPointTimeframe(this.currentRange);

        Observable.zip(
                        this.marketDataService.getBars(symbols, timeframe, limit),
                        this.marketDataService.getQuotes(symbols),
                        (bars, quotes) -> {
                            List<BarData> barData = bars.get(symbol);
                            QuoteData quoteData = quotes.get(symbol);

                            return new StockData(barData, quoteData);
                        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        newStockData -> {
                            List<BarData> barData = ChartHelpers.getSameDayBars(newStockData.getGraphData());
                            this.stockData.updateStock(newStockData, this.currentRange, barData);
                        },
                        err -> Log.e(TAG, "fetchInitialData: " + err.toString()));
    }

    private void fetchGraphData() {
        final int limit = ChartHelpers.getDataPointLimit(this.currentRange);
        final AlpacaTimeframe timeframe = ChartHelpers.getDataPointTimeframe(this.currentRange);

        marketDataService
                .getBars(
                        new Symbols(Collections.singletonList(symbol)),
                        timeframe,
                        limit,
                        ChartHelpers.getIsoStartDate(this.currentRange))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        stockBars -> {
                            List<BarData> bars = stockBars.get(symbol);
                            Log.d(TAG, "newGraphData:" + bars);

                            if (this.currentRange == DateRange.DAY) {
                                bars = ChartHelpers.getSameDayBars(bars);
                            }

                            this.stockData.updateCachedGraphData(this.currentRange, bars);
                        },
                        err -> Log.d(TAG, "fetchGraphData: " + err.getMessage()));
    }

    public int getMaxCandlePoints(int dataSize) {
        switch (this.currentRange) {
            case DAY:
                return 39;
            case WEEK:
            case MONTH:
            case YEAR:
            case THREE_YEARS:
            default:
                return dataSize;
        }
    }

    public int getMaxLinePoints(int dataSize) {
        switch (this.currentRange) {
            case DAY:
                return 78;
            case WEEK:
            case MONTH:
            case YEAR:
            case THREE_YEARS:
            default:
                return dataSize;
        }
    }

    public List<CandleEntry> getCandleStickData() {
        List<BarData> bars = this.stockData.getCachedGraphData(this.currentRange);
        int windowSize = 1;

        switch (this.currentRange) {
            case DAY:
                windowSize = 10;
                break;
            case WEEK:
                windowSize = 4;
                break;
            case MONTH:
                windowSize = 26;
                break;
            case YEAR:
            case THREE_YEARS:
                windowSize = 7;
                break;
        }

        List<BarData> clubbedBars = ChartHelpers.mergeBars(bars, windowSize);

        AtomicInteger i = new AtomicInteger(1);
        this.candleData.clear();
        clubbedBars.forEach(bar -> this.candleData.put(i.getAndIncrement(), bar));

        return this.candleData.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(
                        entry ->
                                new CandleEntry(
                                        entry.getKey(),
                                        entry.getValue().getHigh(),
                                        entry.getValue().getLow(),
                                        entry.getValue().getOpen(),
                                        entry.getValue().getClose()))
                .collect(Collectors.toList());
    }

    public List<Entry> getLineData() {
        List<BarData> bars = this.stockData.getCachedGraphData(this.currentRange);
        int windowSize = 1;

        switch (this.currentRange) {
            case DAY:
                windowSize = 5;
                break;
            case WEEK:
            case YEAR:
            case THREE_YEARS:
                windowSize = 1;
                break;
            case MONTH:
                windowSize = 4;
                break;
        }

        List<BarData> clubbedBars = ChartHelpers.mergeBars(bars, windowSize);

        this.lineData.clear();
        AtomicInteger i = new AtomicInteger(1);
        clubbedBars.forEach(bar -> this.lineData.put(i.getAndIncrement(), bar));

        return this.lineData.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(entry -> new Entry(entry.getKey(), entry.getValue().getClose()))
                .collect(Collectors.toList());
    }
}
