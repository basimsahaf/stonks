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
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.databinding.FragmentStockBinding;
import com.stonks.android.manager.RecentTransactionsManager;
import com.stonks.android.manager.StockManager;
import com.stonks.android.model.*;
import com.stonks.android.model.alpaca.DateRange;
import com.stonks.android.uicomponent.CandleChart;
import com.stonks.android.uicomponent.ChartMarker;
import com.stonks.android.uicomponent.SpeedDialExtendedFab;
import com.stonks.android.uicomponent.StockChart;
import com.stonks.android.utility.Constants;
import com.stonks.android.utility.Formatters;
import java.util.*;

public class StockFragment extends BaseFragment {
    public static final String SYMBOL_ARG = "symbol";
    private static final String TAG = StockFragment.class.getCanonicalName();

    private String symbol;
    private RecyclerView transactionList;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> transactionListAdapter;
    private TextView currentPrice, priceChange;
    private SpeedDialExtendedFab tradeButton;
    private MaterialButton rangeDayButton;
    private MaterialButton rangeWeekButton;
    private MaterialButton rangeMonthButton;
    private MaterialButton rangeYearButton;
    private MaterialButton rangeAllButton;
    private LinearLayout overlay;
    private NestedScrollView scrollView;
    private ImageView changeIndicator;
    private ImageView favIcon;
    private StockChart stockChart;
    private CandleChart candleChart;
    private LineData lineData;
    private LineDataSet lineDataSet;
    private boolean isCandleVisible = true;
    private boolean favourited = false;
    private StockManager stockManager;
    private RecentTransactionsManager transactionsManager;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        FragmentStockBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_stock, container, false);

        this.symbol = getArguments().getString(SYMBOL_ARG);
        this.stockManager = StockManager.getInstance(getMainActivity());
        this.transactionsManager = RecentTransactionsManager.getInstance(getMainActivity());
        this.stockManager.setSymbol(this.symbol);

        // using the addOnPropertyChangedCallback method to update properties that
        // aren't compatible with the @Binding annotation
        // updates the change string, the indicator drawable, and the graphs
        this.stockManager
                .getStockData()
                .addOnPropertyChangedCallback(
                        new androidx.databinding.Observable.OnPropertyChangedCallback() {
                            @Override
                            public void onPropertyChanged(
                                    androidx.databinding.Observable observable, int i) {
                                List<CandleEntry> newCandleData = stockManager.getCandleStickData();
                                LineData lineChartData = stockManager.getLineChartData();

                                priceChange.setText(generateChangeString());
                                changeIndicator.setImageDrawable(getIndicatorDrawable());

                                if (newCandleData != null && newCandleData.size() > 0) {
                                    candleChart.setData(newCandleData);
                                    candleChart.setVisibleXRangeMinimum(
                                            stockManager.getMaxCandlePoints(newCandleData.size()));
                                    candleChart.invalidate();
                                }

                                stockChart.setData(lineChartData);
                                stockChart.setVisibleXRangeMinimum(
                                        stockManager.getMaxLinePoints(
                                                lineChartData.getDataSets().stream()
                                                        .map(IDataSet::getEntryCount)
                                                        .max(Comparator.naturalOrder())
                                                        .orElse(78)));
                                stockChart.invalidate();
                            }
                        });

        binding.setStock(this.stockManager.getStockData());

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

        getMainActivity().subscribe(symbol, this.stockManager.getStockData());
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
                new TransactionViewAdapter(
                        RecentTransactionsFragment.getFakeTransactions(this.symbol));
        this.transactionList.setAdapter(this.transactionListAdapter);

        ChartMarker candleMarker =
                new ChartMarker(
                        getContext(), R.layout.chart_marker, this.stockManager.getCandleMarker());
        candleMarker.setChartView(this.candleChart);
        ChartMarker lineMarker =
                new ChartMarker(
                        getContext(), R.layout.chart_marker, this.stockManager.getLineMarker());
        lineMarker.setChartView(this.stockChart);
        StockChart.CustomGestureListener lineChartGestureListener =
                new StockChart.CustomGestureListener(this.stockChart, this.scrollView);
        StockChart.CustomGestureListener candleChartGestureListener =
                new StockChart.CustomGestureListener(this.candleChart, this.scrollView);
        lineChartGestureListener.setOnGestureEnded(
                () -> {
                    this.currentPrice.setText(
                            Formatters.formatPrice(
                                    this.stockManager.getStockData().getCurrentPrice()));
                    this.priceChange.setText(this.generateChangeString());
                    this.changeIndicator.setImageDrawable(getIndicatorDrawable());
                });
        this.stockChart.setOnScrub(
                (x, y) -> {
                    this.currentPrice.setText(Formatters.formatPrice(y));
                    this.priceChange.setText(this.generateChangeString(y));
                    this.changeIndicator.setImageDrawable(getIndicatorDrawable(y));
                });

        this.lineDataSet = new LineDataSet(Collections.emptyList(), "");
        this.lineData = new LineData(lineDataSet);

        this.stockChart.addValueListener(currentPrice);
        this.stockChart.setOnChartGestureListener(lineChartGestureListener);
        this.stockChart.setMarker(lineMarker);
        this.stockChart.setData(this.lineData);

        this.candleChart.setOnChartGestureListener(candleChartGestureListener);
        this.candleChart.setMarker(candleMarker);
        this.candleChart.setData(Collections.singletonList(new CandleEntry(1, 4f, 2f, 3f, 2.5f)));

        this.tradeButton.setOnClickListener(v -> tradeButton.trigger(this.overlay));
        this.overlay.setOnClickListener(v -> tradeButton.close(v));

        tryButton.setOnClickListener(
                v -> {
                    Fragment hypotheticalFragment = new HypotheticalFragment();
                    Bundle bundle = new Bundle();
                    bundle.putFloat(
                            HypotheticalFragment.CURRENT_PRICE_ARG,
                            this.stockManager.getStockData().getCurrentPrice());
                    hypotheticalFragment.setArguments(bundle);
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.sliding_drawer, hypotheticalFragment, null)
                            .commit();
                });

        buyButton.setOnClickListener(
                v -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(
                            BuySellFragment.STOCK_DATA_ARG, this.stockManager.getStockData());
                    bundle.putSerializable(
                            BuySellFragment.TRANSACTION_MODE_ARG, TransactionMode.BUY);
                    Fragment buyFrag = new BuySellFragment();
                    buyFrag.setArguments(bundle);
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.sliding_drawer, buyFrag, null)
                            .commit();
                });
        sellButton.setOnClickListener(
                v -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(
                            BuySellFragment.STOCK_DATA_ARG, this.stockManager.getStockData());
                    bundle.putSerializable(
                            BuySellFragment.TRANSACTION_MODE_ARG, TransactionMode.BUY);
                    Fragment sellFrag = new BuySellFragment();
                    sellFrag.setArguments(bundle);
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.sliding_drawer, sellFrag, null)
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
                    this.stockManager.setCurrentRange(DateRange.DAY);
                    this.rangeDayButton.setChecked(true);
                });
        rangeWeekButton.setOnClickListener(
                v -> {
                    this.rangeWeekButton.setChecked(true);
                    this.stockManager.setCurrentRange(DateRange.WEEK);
                });
        rangeMonthButton.setOnClickListener(
                v -> {
                    this.stockManager.setCurrentRange(DateRange.MONTH);
                    this.rangeMonthButton.setChecked(true);
                });
        rangeYearButton.setOnClickListener(
                v -> {
                    this.stockManager.setCurrentRange(DateRange.YEAR);
                    this.rangeYearButton.setChecked(true);
                });
        rangeAllButton.setOnClickListener(
                v -> {
                    this.stockManager.setCurrentRange(DateRange.THREE_YEARS);
                    this.rangeAllButton.setChecked(true);
                });

        chartToggleButton.setOnClickListener(v -> this.toggleCandleVisible());

        // default date range is daily
        this.rangeDayButton.setChecked(true);

        if (!this.doesUserPositionExist()) {
            positionContainer.setVisibility(View.GONE);
        }

        this.stockManager.fetchInitialData();
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

    public static ArrayList<Pair<Integer, Float>> getFakeStockPrices() {
        ArrayList<Pair<Integer, Float>> list = new ArrayList<>();
        Float[] prices = Constants.stockDataPoints;

        for (int i = 0; i < prices.length; i += 5) {
            list.add(new Pair<>(i, prices[i]));
        }

        return list;
    }

    SpannableString generateChangeString() {
        return generateChangeString(this.stockManager.getStockData().getCurrentPrice());
    }

    SpannableString generateChangeString(Float price) {
        Pair<Float, Float> changePair = this.stockManager.getChange(price);
        float change = changePair.first;
        float changePercentage = changePair.second;

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
        return getIndicatorDrawable(this.stockManager.getStockData().getCurrentPrice());
    }

    Drawable getIndicatorDrawable(float value) {
        float change = value - this.stockManager.getStockData().getOpen();

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
}
