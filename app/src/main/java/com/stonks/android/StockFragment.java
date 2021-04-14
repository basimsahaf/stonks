package com.stonks.android;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.*;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.databinding.FragmentStockBinding;
import com.stonks.android.manager.FavouriteStocksManager;
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
import java.util.stream.Collectors;

public class StockFragment extends BaseFragment {
    public static final String SYMBOL_ARG = "symbol";
    private static final String TAG = StockFragment.class.getCanonicalName();

    private String symbol;
    private RecyclerView transactionList;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> transactionListAdapter;
    private TextView currentPrice, priceChange;
    private TextView numShares, marketVal, avgCost;
    private TextView todayReturn, totalReturn;
    private SpeedDialExtendedFab tradeButton;
    private MaterialButton rangeDayButton;
    private MaterialButton rangeWeekButton;
    private MaterialButton rangeMonthButton;
    private MaterialButton rangeYearButton;
    private MaterialButton rangeAllButton;
    private ConstraintLayout positionContainer;
    private LinearLayout overlay;
    private View historySection;
    private ImageView changeIndicator;
    private StockChart stockChart;
    private CandleChart candleChart;
    private boolean isCandleVisible = false;
    private StockManager stockManager;
    private RecentTransactionsManager transactionsManager;
    private FavouriteStocksManager favouriteStocksManager;
    private AlertDialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.symbol = getArguments().getString(SYMBOL_ARG);
        this.stockManager = StockManager.getInstance(getMainActivity());
        this.transactionsManager = RecentTransactionsManager.getInstance(getMainActivity());
        this.favouriteStocksManager = FavouriteStocksManager.getInstance(getMainActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.favourite_stock_icon);

        if (this.favouriteStocksManager.isStockFavourited(this.symbol)) {
            item.setIcon(
                    ContextCompat.getDrawable(
                            getMainActivity(), R.drawable.ic_baseline_favorite_48));
        } else {
            item.setIcon(
                    ContextCompat.getDrawable(
                            getMainActivity(), R.drawable.ic_baseline_favorite_border_48));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.favourite_stock_icon) {
            if (this.favouriteStocksManager.isStockFavourited(this.symbol)) {
                this.favouriteStocksManager.removeFavouriteStock(this.symbol);
                item.setIcon(
                        ContextCompat.getDrawable(
                                getMainActivity(), R.drawable.ic_baseline_favorite_border_48));
            } else {
                this.favouriteStocksManager.addFavouriteStock(this.symbol);
                item.setIcon(
                        ContextCompat.getDrawable(
                                getMainActivity(), R.drawable.ic_baseline_favorite_48));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        FragmentStockBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_stock, container, false);
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

                                ArrayList<TransactionsListRow> transactions =
                                        stockManager.getTransactions();
                                PortfolioItem portfolioItem = stockManager.getPosition();

                                if (transactions.size() == 0) {
                                    historySection.setVisibility(View.GONE);
                                } else {
                                    transactionListAdapter =
                                            new TransactionViewAdapter(transactions);
                                    transactionList.setAdapter(transactionListAdapter);

                                    historySection.setVisibility(View.VISIBLE);
                                }

                                if (portfolioItem != null && portfolioItem.getQuantity() != 0) {
                                    positionContainer.setVisibility(View.VISIBLE);

                                    numShares.setText(String.valueOf(portfolioItem.getQuantity()));
                                    marketVal.setText(
                                            Formatters.formatPrice(
                                                    portfolioItem.getQuantity()
                                                            * stockManager
                                                                    .getStockData()
                                                                    .getCurrentPrice()));

                                    List<Transaction> realTransactions =
                                            transactions.stream()
                                                    .filter(
                                                            transactionsListRow ->
                                                                    transactionsListRow
                                                                                    .getTransaction()
                                                                            != null)
                                                    .map(TransactionsListRow::getTransaction)
                                                    .filter(
                                                            transaction ->
                                                                    transaction.getTransactionType()
                                                                            == TransactionMode.BUY)
                                                    .collect(Collectors.toList());

                                    int totalShares = 0;
                                    float cost = 0f;

                                    for (Transaction transaction : realTransactions) {
                                        totalShares += transaction.getShares();
                                        cost += transaction.getShares() * transaction.getPrice();
                                    }

                                    float averageCost = cost / totalShares;
                                    avgCost.setText(Formatters.formatPrice(averageCost));

                                    float returnTotal =
                                            (stockManager.getStockData().getCurrentPrice()
                                                            * totalShares)
                                                    - cost;
                                    float returnToday =
                                            (stockManager.getStockData().getCurrentPrice()
                                                            - stockManager.getStockData().getOpen())
                                                    * totalShares;

                                    todayReturn.setText(Formatters.formatPrice(returnToday));
                                    totalReturn.setText(Formatters.formatPrice(returnTotal));
                                } else {
                                    positionContainer.setVisibility(View.GONE);
                                }

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
        final FloatingActionButton tryButton = view.findViewById(R.id.try_button);
        final FloatingActionButton buyButton = view.findViewById(R.id.buy_button);
        final FloatingActionButton sellButton = view.findViewById(R.id.sell_button);
        final ImageButton chartToggleButton = view.findViewById(R.id.chart_toggle);
        final MaterialButton indicatorButton = view.findViewById(R.id.indicator_button);
        final NestedScrollView scrollView = view.findViewById(R.id.scroll_view);

        this.positionContainer = view.findViewById(R.id.position_container);
        this.numShares = view.findViewById(R.id.num_shares);
        this.marketVal = view.findViewById(R.id.mkt_value);
        this.avgCost = view.findViewById(R.id.avg_cost);
        this.todayReturn = view.findViewById(R.id.day_return);
        this.totalReturn = view.findViewById(R.id.total_return);
        this.overlay = view.findViewById(R.id.screen_overlay);
        this.tradeButton = view.findViewById(R.id.trade_button);
        this.transactionList = view.findViewById(R.id.history_list);
        this.currentPrice = view.findViewById(R.id.current_price);
        this.priceChange = view.findViewById(R.id.change);
        this.changeIndicator = view.findViewById(R.id.change_indicator);
        this.stockChart = view.findViewById(R.id.stock_chart);
        this.candleChart = view.findViewById(R.id.stock_chart_candle);
        this.historySection = view.findViewById(R.id.history_section);
        this.rangeDayButton = view.findViewById(R.id.range_day);
        this.rangeWeekButton = view.findViewById(R.id.range_week);
        this.rangeMonthButton = view.findViewById(R.id.range_month);
        this.rangeYearButton = view.findViewById(R.id.range_year);
        this.rangeAllButton = view.findViewById(R.id.range_all);

        this.tradeButton.addToSpeedDial(buyButtonContainer);
        this.tradeButton.addToSpeedDial(sellButtonContainer);
        this.tradeButton.addToSpeedDial(tryButtonContainer);

        MaterialAlertDialogBuilder dialogBuilder =
                new MaterialAlertDialogBuilder(getMainActivity());
        dialogBuilder
                .setView(R.layout.indicator_configuration)
                .setPositiveButton(
                        "Done",
                        (dialogInterface, i) -> {
                            final MaterialCheckBox maEnabled =
                                    this.dialog.findViewById(R.id.moving_average_enabled);
                            final MaterialCheckBox wmaEnabled =
                                    this.dialog.findViewById(R.id.weighted_moving_average_enabled);

                            final TextInputEditText maPeriod =
                                    this.dialog.findViewById(R.id.moving_average_period);
                            final TextInputEditText wmaPeriod =
                                    this.dialog.findViewById(R.id.weighted_moving_average_period);

                            this.stockManager.setMovingAverage(
                                    maEnabled.isChecked(),
                                    Integer.parseInt(maPeriod.getText().toString()));
                            this.stockManager.setWMovingAverageEnabled(
                                    wmaEnabled.isChecked(),
                                    Integer.parseInt(wmaPeriod.getText().toString()));
                        })
                .setNegativeButton("Cancel", null);
        this.dialog = dialogBuilder.create();

        indicatorButton.setOnClickListener(
                v -> {
                    dialog.show();

                    final MaterialCheckBox maEnabled =
                            dialog.findViewById(R.id.moving_average_enabled);
                    final MaterialCheckBox wmaEnabled =
                            dialog.findViewById(R.id.weighted_moving_average_enabled);

                    final TextInputEditText maPeriod =
                            dialog.findViewById(R.id.moving_average_period);
                    final TextInputEditText wmaPeriod =
                            dialog.findViewById(R.id.weighted_moving_average_period);

                    maEnabled.setChecked(this.stockManager.isMovingAverageEnabled());
                    wmaEnabled.setChecked(this.stockManager.iswMovingAverageEnabled());
                    maPeriod.setText("" + this.stockManager.getMovingAveragePeriod());
                    wmaPeriod.setText("" + this.stockManager.getWeightedMovingAveragePeriod());
                });

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
                new StockChart.CustomGestureListener(this.stockChart, scrollView);
        StockChart.CustomGestureListener candleChartGestureListener =
                new StockChart.CustomGestureListener(this.candleChart, scrollView);
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

        LineDataSet lineDataSet = new LineDataSet(Collections.emptyList(), "");
        LineData lineData = new LineData(lineDataSet);

        this.stockChart.addValueListener(currentPrice);
        this.stockChart.setOnChartGestureListener(lineChartGestureListener);
        this.stockChart.setMarker(lineMarker);
        this.stockChart.setData(lineData);

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
                            BuySellFragment.TRANSACTION_MODE_ARG, TransactionMode.SELL);
                    Fragment sellFrag = new BuySellFragment();
                    sellFrag.setArguments(bundle);
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.sliding_drawer, sellFrag, null)
                            .commit();
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

        chartToggleButton.setOnClickListener(
                v -> {
                    this.toggleCandleVisible();
                    if (this.isCandleVisible) {
                        chartToggleButton.setImageDrawable(
                                ContextCompat.getDrawable(
                                        getMainActivity(), R.drawable.ic_baseline_show_chart_24));
                    } else {
                        chartToggleButton.setImageDrawable(
                                ContextCompat.getDrawable(
                                        getMainActivity(), R.drawable.ic_baseline_graphic_eq_24));
                    }
                });

        // default date range is daily
        this.rangeDayButton.setChecked(true);

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

    Drawable getIndicatorDrawable(float price) {
        Pair<Float, Float> changePair = this.stockManager.getChange(price);
        float change = changePair.first;

        if (change >= 0) {
            return ContextCompat.getDrawable(
                    getMainActivity(), R.drawable.ic_baseline_arrow_drop_up_24);
        } else {
            return ContextCompat.getDrawable(
                    getMainActivity(), R.drawable.ic_baseline_arrow_drop_down_24);
        }
    }
}
