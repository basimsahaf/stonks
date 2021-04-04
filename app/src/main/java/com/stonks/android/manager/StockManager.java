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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StockManager {
    private static final String TAG = StockManager.class.getCanonicalName();
    private final String symbol;
    private final StockData stockData;
    private final MarketDataService marketDataService;

    public StockManager(String symbol) {
        this.symbol = symbol;
        this.stockData = new StockData();
        // TODO: use singleton
        this.marketDataService = new MarketDataService();
    }

    private void fetchInitialData() {
        final Symbols symbols = new Symbols(Collections.singletonList(this.symbol));

        Observable.zip(
                        this.marketDataService.getBars(AlpacaTimeframe.MINUTE, symbols),
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
                            List<BarData> barData = newStockData.getGraphData();
                            this.stockData.updateStock(newStockData);

                            AtomicInteger i = new AtomicInteger();
                            List<Entry> dataSet =
                                    barData.stream()
                                            .map(
                                                    bar ->
                                                            new Entry(
                                                                    i.getAndIncrement(),
                                                                    bar.getClose()))
                                            .collect(Collectors.toList());

                            this.stockChart.setData(dataSet);
                            this.stockChart.setLimitLine(barData.get(0).getOpen());
                            this.stockChart.invalidate();

                            List<BarData> clubbedBars = new ArrayList<>();
                            for (int x = 0; x < barData.size(); x += 5) {
                                BarData newBar =
                                        clubBars(
                                                barData.subList(
                                                        x, Math.min(x + 4, barData.size() - 1)));

                                clubbedBars.add(newBar);
                            }

                            i.set(1);
                            this.candleChart.setData(
                                    clubbedBars.stream()
                                            .map(bar -> bar.toCandleEntry(i.getAndIncrement()))
                                            .collect(Collectors.toList()));
                            this.stockChart.setLimitLine(clubbedBars.get(0).getOpen());
                            this.candleChart.invalidate();
                        },
                        err -> Log.e(TAG, err.toString()));
    }

    private List<CandleEntry> getCandleStickData() {
        switch (timeframe) {
            case DAY:
            case MINUTE:
            case MINUTES_5:
            case MINUTES_15:
        }
    }
}
