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
import java.util.stream.Collectors;

public class StockManager {
    private static final String TAG = StockManager.class.getCanonicalName();
    private final String symbol;
    private final StockData stockData;
    private final MarketDataService marketDataService;
    private DateRange currentRange;

    public StockManager(String symbol) {
        this.symbol = symbol;
        this.stockData = new StockData();
        // TODO: use singleton
        this.marketDataService = new MarketDataService();
        this.currentRange = DateRange.DAY;
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

        Observable.zip(
                        this.marketDataService.getBars(symbols, AlpacaTimeframe.MINUTE, 390),
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
                            List<BarData> barData = getSameDayBars(newStockData.getGraphData());
                            this.stockData.updateStock(newStockData, this.currentRange, barData);
                        },
                        err -> Log.e(TAG, "API error: " + err.toString()));
    }

    public List<BarData> fetchGraphData() {
        int limit;
        AlpacaTimeframe timeframe;

        switch (this.currentRange) {
            case WEEK:
                limit = 390;
                timeframe = AlpacaTimeframe.MINUTES_15;
                break;
            case MONTH:
                limit = 217;
                timeframe = AlpacaTimeframe.MINUTES_15;
                break;
            case YEAR:
                limit = 365;
                timeframe = AlpacaTimeframe.DAY;
                break;
            case THREE_YEARS:
                limit = 1000;
                timeframe = AlpacaTimeframe.DAY;
                break;
            case DAY:
            default:
                limit = 390;
                timeframe = AlpacaTimeframe.MINUTE;
        }

        Log.d(
                TAG,
                "fetchGraph query params"
                        + timeframe
                        + " "
                        + limit
                        + " "
                        + getIsoStartDate(this.currentRange));

        marketDataService
                .getBars(
                        new Symbols(Collections.singletonList(symbol)),
                        timeframe,
                        limit,
                        getIsoStartDate(this.currentRange))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        stockBars -> {
                            List<BarData> bars = stockBars.get(symbol);
                            Log.d(TAG, "newGraphData:" + bars);

                            if (this.currentRange == DateRange.DAY) {
                                bars = getSameDayBars(bars);
                            }

                            this.stockData.updateCachedGraphData(this.currentRange, bars);
                        },
                        err -> Log.d(TAG, "fetchGraphData: " + err.getMessage()));

        return Collections.emptyList();
    }

    private String getIsoStartDate(DateRange range) {
        if (range == DateRange.DAY) {
            // for a single day chart, we fetch 390 data points and then filter
            return "";
        }

        LocalDateTime now = LocalDateTime.now();

        switch (range) {
            case WEEK:
                now = now.minusDays(7);
                break;
            case MONTH:
                now = now.minusMonths(1);
                break;
            case YEAR:
                now = now.minusYears(1);
                break;
            case THREE_YEARS:
                now = now.minusYears(3);
                break;
        }

        ZonedDateTime dateTime =
                ZonedDateTime.ofInstant(
                        now.atZone(TimeZone.getDefault().toZoneId()).toInstant(),
                        ZoneId.systemDefault());
        DateTimeFormatter df = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        return df.format(dateTime);
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

    private BarData clubBars(List<BarData> bars) {
        BarData barData = new BarData(-1, -1, -1, -1, -1);

        barData.setHigh(
                bars.stream().map(BarData::getHigh).max(Comparator.naturalOrder()).orElse(0f));
        barData.setLow(
                bars.stream().map(BarData::getLow).min(Comparator.naturalOrder()).orElse(0f));
        barData.setOpen(bars.get(0).getOpen());
        barData.setTimestamp(bars.get(0).getTimestamp());
        barData.setClose(bars.get(bars.size() - 1).getClose());
        barData.setEndTimestamp(bars.get(bars.size() - 1).getTimestamp());

        Log.d(TAG, "clubBars complete");

        return barData;
    }

    private List<BarData> getSameDayBars(List<BarData> bars) {
        BarData lastBar = bars.get(bars.size() - 1);
        LocalDateTime day =
                LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(lastBar.getTimestamp()),
                        TimeZone.getDefault().toZoneId());

        LocalDateTime firstEntry = day.withHour(9).withMinute(30).withSecond(0);
        long firstTimeStamp = firstEntry.atZone(TimeZone.getDefault().toZoneId()).toEpochSecond();

        Log.d(TAG, "FirstEntry: " + firstTimeStamp);

        List<BarData> sameDayBars = new ArrayList<>();

        for (BarData bar : bars) {
            if (bar.getTimestamp() < firstTimeStamp) continue;

            sameDayBars.add(bar);
        }

        return sameDayBars;
    }

    public List<CandleEntry> getCandleStickData() {
        List<BarData> bars = this.stockData.getCachedGraphData(this.currentRange);
        Log.d(TAG, "getCandle: " + bars);
        List<BarData> clubbedBars = new ArrayList<>();
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

        for (int x = 0; x < bars.size(); x += windowSize) {
            int endIdx = Math.min(x + windowSize, bars.size() - 1);

            if (x == endIdx) break;

            BarData newBar = clubBars(bars.subList(x, endIdx));

            clubbedBars.add(newBar);
        }

        AtomicInteger i = new AtomicInteger(1);
        return clubbedBars.stream()
                .map(
                        bar ->
                                new CandleEntry(
                                        i.getAndIncrement(),
                                        bar.getHigh(),
                                        bar.getLow(),
                                        bar.getOpen(),
                                        bar.getClose()))
                .collect(Collectors.toList());
    }

    public List<Entry> getLineData() {
        List<BarData> bars = this.stockData.getCachedGraphData(this.currentRange);
        Log.d(TAG, "getLine: " + bars);
        List<BarData> clubbedBars = new ArrayList<>();
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

        for (int x = 0; x < bars.size(); x += windowSize) {
            int endIdx = Math.min(x + windowSize, bars.size() - 1);

            if (x == endIdx) break;

            BarData newBar = clubBars(bars.subList(x, endIdx));

            clubbedBars.add(newBar);
        }

        AtomicInteger i = new AtomicInteger(1);
        return clubbedBars.stream()
                .map(bar -> new Entry(i.getAndIncrement(), bar.getClose()))
                .collect(Collectors.toList());
    }
}
