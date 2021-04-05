package com.stonks.android;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.databinding.FragmentStockBinding;
import com.stonks.android.external.MarketDataService;
import com.stonks.android.manager.StockManager;
import com.stonks.android.model.*;
import com.stonks.android.model.alpaca.DateRange;
import com.stonks.android.uicomponent.CandleChart;
import com.stonks.android.uicomponent.ChartMarker;
import com.stonks.android.uicomponent.SpeedDialExtendedFab;
import com.stonks.android.uicomponent.StockChart;
import com.stonks.android.utility.Constants;
import com.stonks.android.utility.Formatters;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

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
    private CandleChart candleChart;

    private boolean favourited = false;
    private DateRange currentDateRange;

    private Symbols symbols;
    private final MarketDataService marketDataService = new MarketDataService();
    private StockManager manager;
    private boolean isCandleVisible = true;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        FragmentStockBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_stock, container, false);

        this.symbol = getArguments().getString(getString(R.string.intent_extra_symbol));
        this.symbols = new Symbols(Collections.singletonList(symbol));

        this.manager = new StockManager(this.symbol);
        // update properties like change string and change indicator
        this.manager
                .getStockData()
                .addOnPropertyChangedCallback(
                        new androidx.databinding.Observable.OnPropertyChangedCallback() {
                            @Override
                            public void onPropertyChanged(
                                    androidx.databinding.Observable observable, int i) {
                                priceChange.setText(
                                        generateChangeString(
                                                manager.getStockData().getCurrentPrice()));
                                changeIndicator.setImageDrawable(getIndicatorDrawable());

                                List<CandleEntry> newCandleData = manager.getCandleStickData();
                                List<Entry> newLineData = manager.getLineData();

                                if (newCandleData != null && newCandleData.size() > 0) {
                                    candleChart.setData(newCandleData);
                                    candleChart.setVisibleXRangeMinimum(
                                            manager.getMaxCandlePoints(newCandleData.size()));
                                    candleChart.invalidate();
                                }

                                if (newLineData != null && newLineData.size() > 0) {
                                    stockChart.setData(newLineData);
                                    stockChart.setVisibleXRangeMinimum(
                                            manager.getMaxLinePoints(newLineData.size()));
                                    stockChart.invalidate();
                                }
                            }
                        });

        binding.setStock(this.manager.getStockData());

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();

        // unsubscribe from WebSocket updates when the fragment is paused
        getMainActivity().unsubscribe(this.symbol);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getMainActivity().subscribe(symbol, this.manager.getStockData());
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getMainActivity().setGlobalTitle(this.symbol);

        final ConstraintLayout buyButtonContainer = view.findViewById(R.id.buy_button_container);
        final ConstraintLayout sellButtonContainer = view.findViewById(R.id.sell_button_container);
        final ConstraintLayout tryButtonContainer = view.findViewById(R.id.try_button_container);
        final ConstraintLayout positionContainer = view.findViewById(R.id.position_container);
        final FloatingActionButton tryButton = view.findViewById(R.id.try_button);
        final FloatingActionButton buyButton = view.findViewById(R.id.buy_button);
        final FloatingActionButton sellButton = view.findViewById(R.id.sell_button);
        final ImageView chartToggleButton = view.findViewById(R.id.chart_toggle);

        this.favIcon = view.findViewById(R.id.fav_icon);
        this.overlay = view.findViewById(R.id.screen_overlay);
        this.tradeButton = view.findViewById(R.id.trade_button);
        this.transactionList = view.findViewById(R.id.history_list);
        this.scrollView = view.findViewById(R.id.scroll_view);
        this.currentPrice = view.findViewById(R.id.current_price);
        this.priceChange = view.findViewById(R.id.change);
        this.changeIndicator = view.findViewById(R.id.change_indicator);
        this.stockChart = view.findViewById(R.id.stock_chart);
        this.candleChart = view.findViewById(R.id.stock_chart_candle);

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

        ChartMarker marker = new ChartMarker(getContext(), R.layout.chart_marker);
        marker.setChartView(this.candleChart);
        StockChart.CustomGestureListener lineChartGestureListener =
                new StockChart.CustomGestureListener(this.stockChart, this.scrollView);
        StockChart.CustomGestureListener candleChartGestureListener =
                new StockChart.CustomGestureListener(this.candleChart, this.scrollView);
        lineChartGestureListener.setOnGestureEnded(
                () -> {
                    this.currentPrice.setText(
                            Formatters.formatPrice(this.manager.getStockData().getCurrentPrice()));
                    this.priceChange.setText(this.generateChangeString());
                    this.changeIndicator.setImageDrawable(getIndicatorDrawable());
                });
        this.stockChart.setOnScrub(
                (x, y) -> {
                    this.currentPrice.setText(Formatters.formatPrice(y));
                    this.priceChange.setText(this.generateChangeString(y));
                    this.changeIndicator.setImageDrawable(getIndicatorDrawable(y));
                });

        this.stockChart.addValueListener(currentPrice);
        this.stockChart.setOnChartGestureListener(lineChartGestureListener);
        this.stockChart.setData(Collections.emptyList());

        this.candleChart.setOnChartGestureListener(candleChartGestureListener);
        this.candleChart.setMarker(marker);
        this.candleChart.setData(Collections.singletonList(new CandleEntry(1, 4f, 2f, 3f, 2.5f)));

        this.tradeButton.setOnClickListener(v -> tradeButton.trigger(this.overlay));
        this.overlay.setOnClickListener(v -> tradeButton.close(v));

        tryButton.setOnClickListener(
                v -> {
                    Fragment hypotheticalFragment = new HypotheticalFragment();
                    Bundle bundle = new Bundle();
                    bundle.putFloat(
                            HypotheticalFragment.CURRENT_PRICE_ARG,
                            this.manager.getStockData().getCurrentPrice());
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
                    bundle.putSerializable(
                            BuySellFragment.STOCK_DATA_ARG, this.manager.getStockData());
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
                    bundle.putSerializable(
                            BuySellFragment.STOCK_DATA_ARG, this.manager.getStockData());
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
                    this.manager.setCurrentRange(DateRange.DAY);
                    switchDateRange(DateRange.DAY);
                });
        rangeWeekButton.setOnClickListener(
                v -> {
                    switchDateRange(DateRange.WEEK);
                    this.manager.setCurrentRange(DateRange.WEEK);
                });
        rangeMonthButton.setOnClickListener(
                v -> {
                    switchDateRange(DateRange.MONTH);
                    this.manager.setCurrentRange(DateRange.MONTH);
                });
        rangeYearButton.setOnClickListener(
                v -> {
                    switchDateRange(DateRange.YEAR);
                    this.manager.setCurrentRange(DateRange.YEAR);
                });
        rangeAllButton.setOnClickListener(
                v -> {
                    switchDateRange(DateRange.THREE_YEARS);
                    this.manager.setCurrentRange(DateRange.THREE_YEARS);
                });

        chartToggleButton.setOnClickListener(v -> this.toggleCandleVisible());

        // default date range is daily
        this.rangeDayButton.setChecked(true);
        this.currentDateRange = DateRange.DAY;

        if (!this.doesUserPositionExist()) {
            positionContainer.setVisibility(View.GONE);
        }

        this.manager.fetchInitialData();
    }

    private void toggleCandleVisible() {
        if (this.isCandleVisible) {
            this.stockChart.setVisibility(View.VISIBLE);
            this.candleChart.setVisibility(View.GONE);
        } else {
            this.stockChart.setVisibility(View.GONE);
            this.candleChart.setVisibility(View.VISIBLE);
        }

        this.isCandleVisible = !this.isCandleVisible;
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
        return this.generateChangeString(this.manager.getStockData().getCurrentPrice());
    }

    SpannableString generateChangeString(Float price) {
        float change = price - this.manager.getStockData().getOpen();
        float changePercentage = change * 100 / this.manager.getStockData().getOpen();

        String formattedPrice = Formatters.formatPrice(Math.abs(change));
        String changeString =
                String.format(
                        Locale.CANADA, "%s (%.2f%%)", formattedPrice, Math.abs(changePercentage));
        SpannableString text = new SpannableString(changeString);

        int color = change >= 0 ? R.color.green : R.color.red;

        text.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(getContext(), color)),
                0,
                changeString.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        return text;
    }

    Drawable getIndicatorDrawable() {
        return getIndicatorDrawable(this.manager.getStockData().getCurrentPrice());
    }

    Drawable getIndicatorDrawable(float value) {
        float change = value - this.manager.getStockData().getOpen();

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
