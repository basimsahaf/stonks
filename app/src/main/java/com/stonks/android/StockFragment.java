package com.stonks.android;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.databinding.FragmentStockBinding;
import com.stonks.android.external.MarketDataService;
import com.stonks.android.model.*;
import com.stonks.android.model.alpaca.AlpacaTimeframe;
import com.stonks.android.model.alpaca.DateRange;
import com.stonks.android.uicomponent.SpeedDialExtendedFab;
import com.stonks.android.uicomponent.StockChart;
import com.stonks.android.utility.Constants;
import com.stonks.android.utility.Formatters;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StockFragment extends BaseFragment {
    private static String TAG = StockFragment.class.getCanonicalName();

    private String symbol;
    private RecyclerView transactionList;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> transactionListAdapter;
    private TextView currentPrice, priceChange;
    private SpeedDialExtendedFab tradeButton;
    private MaterialButton rangeDayButton,
            rangeWeekButton,
            rangeMonthButton,
            rangeYearButton,
            rangeAllButton;
    private LinearLayout overlay;
    private NestedScrollView scrollView;
    private ImageView changeIndicator;
    private ImageView favIcon;
    private StockChart stockChart;

    private boolean favourited = false;
    private final StockData stockData = new StockData();
    private DateRange currentDateRange;

    private Symbols symbols;
    private final MarketDataService marketDataService = new MarketDataService();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        FragmentStockBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_stock, container, false);

        // update properties like change string and change indicator
        stockData.addOnPropertyChangedCallback(
                new androidx.databinding.Observable.OnPropertyChangedCallback() {
                    @Override
                    public void onPropertyChanged(
                            androidx.databinding.Observable observable, int i) {
                        priceChange.setText(generateChangeString(stockData.getCurrentPrice()));
                        changeIndicator.setImageDrawable(getIndicatorDrawable());
                    }
                });

        binding.setStock(stockData);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.symbol = getArguments().getString(getString(R.string.intent_extra_symbol));
        symbols = new Symbols(Collections.singletonList(symbol));

        getMainActivity().subscribe(symbol, stockData);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getMainActivity().setGlobalTitle(this.symbol);

        final ConstraintLayout buyButtonContainer = view.findViewById(R.id.buy_button_container);
        final ConstraintLayout sellButtonContainer = view.findViewById(R.id.sell_button_container);
        final ConstraintLayout tryButtonContainer = view.findViewById(R.id.try_button_container);
        final ConstraintLayout positionContainer = view.findViewById(R.id.position_container);
        final FloatingActionButton tryButton = view.findViewById(R.id.try_button);
        final FloatingActionButton buyButton = view.findViewById(R.id.buy_button);
        final FloatingActionButton sellButton = view.findViewById(R.id.sell_button);

        this.favIcon = view.findViewById(R.id.fav_icon);
        this.overlay = view.findViewById(R.id.screen_overlay);
        this.tradeButton = view.findViewById(R.id.trade_button);
        this.transactionList = view.findViewById(R.id.history_list);
        this.scrollView = view.findViewById(R.id.scroll_view);
        this.currentPrice = view.findViewById(R.id.current_price);
        this.priceChange = view.findViewById(R.id.change);
        this.changeIndicator = view.findViewById(R.id.change_indicator);
        this.stockChart = view.findViewById(R.id.stock_chart);

        this.rangeDayButton = view.findViewById(R.id.range_day);
        this.rangeWeekButton = view.findViewById(R.id.range_week);
        this.rangeMonthButton = view.findViewById(R.id.range_month);
        this.rangeYearButton = view.findViewById(R.id.range_year);
        this.rangeAllButton = view.findViewById(R.id.range_all);

        this.tradeButton.addToSpeedDial(buyButtonContainer);
        this.tradeButton.addToSpeedDial(sellButtonContainer);
        this.tradeButton.addToSpeedDial(tryButtonContainer);

        RecyclerView.LayoutManager transactionListManager = new LinearLayoutManager(getContext());
        this.transactionList.setLayoutManager(transactionListManager);
        this.transactionListAdapter =
                new TransactionViewAdapter(this.getFakeTransactionsForStock());
        this.transactionList.setAdapter(this.transactionListAdapter);

        StockChart.CustomGestureListener customGestureListener =
                new StockChart.CustomGestureListener(this.stockChart, this.scrollView);
        this.stockChart.setOnScrub(
                (x, y) -> {
                    currentPrice.setText(Formatters.formatPrice(y));
                    this.priceChange.setText(this.generateChangeString(y));
                    this.changeIndicator.setImageDrawable(getIndicatorDrawable(y));
                });
        customGestureListener.setOnGestureEnded(
                () -> {
                    this.currentPrice.setText(Formatters.formatPrice(stockData.getCurrentPrice()));
                    this.priceChange.setText(this.generateChangeString());
                    this.changeIndicator.setImageDrawable(getIndicatorDrawable());
                });
        this.stockChart.addValueListener(currentPrice);
        this.stockChart.setOnChartGestureListener(customGestureListener);
        this.stockChart.setData(Collections.emptyList());

        this.tradeButton.setOnClickListener(v -> tradeButton.trigger(this.overlay));
        this.overlay.setOnClickListener(v -> tradeButton.close(v));

        tryButton.setOnClickListener(
                v -> {
                    Fragment hypotheticalFragment = new HypotheticalFragment();
                    Bundle bundle = new Bundle();
                    bundle.putFloat(
                            HypotheticalFragment.CURRENT_PRICE_ARG, stockData.getCurrentPrice());
                    hypotheticalFragment.setArguments(bundle);
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.sliding_drawer, hypotheticalFragment, null)
                            .addToBackStack(null)
                            .commit();
                });

        buyButton.setOnClickListener(
                v -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(BuySellFragment.STOCK_DATA_ARG, this.stockData);
                    bundle.putSerializable(
                            BuySellFragment.TRANSACTION_MODE_ARG, TransactionMode.BUY);
                    Fragment buyFrag = new BuySellFragment();
                    buyFrag.setArguments(bundle);
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.sliding_drawer, buyFrag, null)
                            .addToBackStack(null)
                            .commit();
                });
        sellButton.setOnClickListener(
                v -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(BuySellFragment.STOCK_DATA_ARG, this.stockData);
                    bundle.putSerializable(
                            BuySellFragment.TRANSACTION_MODE_ARG, TransactionMode.BUY);
                    Fragment sellFrag = new BuySellFragment();
                    sellFrag.setArguments(bundle);
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.sliding_drawer, sellFrag, null)
                            .addToBackStack(null)
                            .commit();
                });
        this.favIcon.setOnClickListener(
                v -> {
                    favourited = !favourited;

                    if (favourited) {
                        favIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                        getContext(), R.drawable.ic_baseline_favorite_48));
                    } else {
                        favIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                        getContext(), R.drawable.ic_baseline_favorite_border_48));
                    }
                });

        rangeDayButton.setOnClickListener(
                v -> {
                    switchDateRange(DateRange.DAY);
                });
        rangeWeekButton.setOnClickListener(
                v -> {
                    switchDateRange(DateRange.WEEK);
                });
        rangeMonthButton.setOnClickListener(
                v -> {
                    switchDateRange(DateRange.MONTH);
                });
        rangeYearButton.setOnClickListener(
                v -> {
                    switchDateRange(DateRange.YEAR);
                });
        rangeAllButton.setOnClickListener(
                v -> {
                    switchDateRange(DateRange.THREE_YEARS);
                });

        // default date range is daily
        this.rangeDayButton.setChecked(true);
        this.currentDateRange = DateRange.DAY;

        if (!this.doesUserPositionExist()) {
            positionContainer.setVisibility(View.GONE);
        }
        this.fetchInitialData();
    }

    private void fetchInitialData() {
        AlpacaTimeframe timeframe;
        int limit;

        switch (this.currentDateRange) {
            case DAY:
                limit = 390; // 390 trading hours in a day
                timeframe = AlpacaTimeframe.MINUTE;
                break;
            case WEEK:
                limit = 390; // 390 * 5 days / 5 min increments
                timeframe = AlpacaTimeframe.MINUTES_5;
                break;
            case MONTH:
                limit = 806; // 390 * 31 days / 15 min increments
                timeframe = AlpacaTimeframe.MINUTES_15;
                break;
            case YEAR:
                limit = 366;
                timeframe = AlpacaTimeframe.DAY;
                break;
            case THREE_YEARS:
                limit = 1000;
                timeframe = AlpacaTimeframe.DAY;
                break;
            default:
                limit = 100;
                timeframe = AlpacaTimeframe.MINUTE;
                break;
        }

        Observable.zip(
                        marketDataService.getBars(symbols, timeframe, limit),
                        marketDataService.getQuotes(symbols),
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
                        },
                        err -> Log.e(TAG, err.toString()));
    }

    private void switchDateRange(DateRange dateRange) {
        switch (dateRange) {
            case WEEK:
                this.currentDateRange = DateRange.WEEK;
                this.rangeWeekButton.setChecked(true);
                break;
            case MONTH:
                this.currentDateRange = DateRange.MONTH;
                this.rangeMonthButton.setChecked(true);
                break;
            case YEAR:
                this.currentDateRange = DateRange.YEAR;
                this.rangeYearButton.setChecked(true);
                break;
            case THREE_YEARS:
                this.currentDateRange = DateRange.THREE_YEARS;
                this.rangeAllButton.setChecked(true);
                break;
            default:
                this.currentDateRange = DateRange.DAY;
                this.rangeDayButton.setChecked(true);
        }
        this.fetchInitialData();
    }

    public static ArrayList<Pair<Integer, Float>> getFakeStockPrices() {
        ArrayList<Pair<Integer, Float>> list = new ArrayList<>();
        Float[] prices = Constants.stockDataPoints;

        for (int i = 0; i < prices.length; i += 5) {
            list.add(new Pair<>(i, prices[i]));
        }

        return list;
    }

    SpannableString generateChangeString() {
        return this.generateChangeString(this.stockData.getCurrentPrice());
    }

    SpannableString generateChangeString(Float price) {
        float change = price - this.stockData.getOpen();
        float changePercentage = change * 100 / this.stockData.getOpen();

        String formattedPrice = Formatters.formatPrice(Math.abs(change));
        String changeString =
                String.format(
                        Locale.CANADA, "%s (%.2f%%)", formattedPrice, Math.abs(changePercentage));
        SpannableString text = new SpannableString(changeString);

        int color = change >= 0 ? R.color.green : R.color.red;

        text.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(getMainActivity(), color)),
                0,
                changeString.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        return text;
    }

    Drawable getIndicatorDrawable() {
        return getIndicatorDrawable(stockData.getCurrentPrice());
    }

    Drawable getIndicatorDrawable(float value) {
        float change = value - stockData.getOpen();

        if (change >= 0) {
            return ContextCompat.getDrawable(
                    getMainActivity(), R.drawable.ic_baseline_arrow_drop_up_24);
        } else {
            return ContextCompat.getDrawable(
                    getMainActivity(), R.drawable.ic_baseline_arrow_drop_down_24);
        }
    }

    // Determines whether the user owns shares of the stock
    // Hard coded to false for now
    private boolean doesUserPositionExist() {
        return false;
    }

    public ArrayList<TransactionsListRow> getFakeTransactionsForStock() {
        ArrayList<TransactionsListRow> list = new ArrayList<>();

        list.add(new TransactionsListRow(LocalDateTime.of(2020, Month.AUGUST, 19, 13, 14)));
        list.add(
                new TransactionsListRow(
                        new Transaction(
                                "username",
                                this.symbol,
                                100,
                                56.92f,
                                TransactionMode.BUY,
                                LocalDateTime.of(2020, Month.AUGUST, 19, 13, 14))));
        list.add(
                new TransactionsListRow(
                        new Transaction(
                                "username",
                                this.symbol,
                                268,
                                36.47f,
                                TransactionMode.BUY,
                                LocalDateTime.of(2020, Month.AUGUST, 1, 9, 52))));

        return list;
    }

    public static ArrayList<TransactionsListRow> getFakeTransactions() {
        ArrayList<TransactionsListRow> list = new ArrayList<>();

        list.add(new TransactionsListRow(LocalDateTime.of(2020, Month.AUGUST, 19, 13, 14)));
        list.add(
                new TransactionsListRow(
                        new Transaction(
                                "username",
                                "SHOP",
                                100,
                                56.92f,
                                TransactionMode.BUY,
                                LocalDateTime.of(2020, Month.AUGUST, 19, 13, 14))));
        list.add(
                new TransactionsListRow(
                        new Transaction(
                                "username",
                                "SHOP",
                                268,
                                36.47f,
                                TransactionMode.BUY,
                                LocalDateTime.of(2020, Month.AUGUST, 1, 9, 52))));

        return list;
    }
}
