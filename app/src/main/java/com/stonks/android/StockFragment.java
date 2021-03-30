package com.stonks.android;

import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stonks.android.adapter.StockChartAdapter;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.external.MarketDataService;
import com.stonks.android.model.*;
import com.stonks.android.uicomponent.CustomSparkView;
import com.stonks.android.uicomponent.SpeedDialExtendedFab;
import com.stonks.android.utility.Constants;
import com.stonks.android.utility.Formatters;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class StockFragment extends BaseFragment {
    private static String TAG = StockFragment.class.getCanonicalName();

    private String symbol;
    private RecyclerView transactionList;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> transactionListAdapter;
    private TextView textViewSymbol, currentPrice, priceChange;
    private SpeedDialExtendedFab tradeButton;
    private LinearLayout overlay;
    private NestedScrollView scrollView;
    private final StockChartAdapter dataAdapter = new StockChartAdapter(new ArrayList<>());
    private StockData stockData;
    private TextView companyName, open, dailyLow, dailyHigh, yearlyLow, yearlyHigh;
    private FloatingActionButton tryButton;
    private FloatingActionButton buyButton;
    private FloatingActionButton sellButton;
    private ImageView favIcon;

    private boolean favourited = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stock, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ConstraintLayout buyButtonContainer = view.findViewById(R.id.buy_button_container);
        final ConstraintLayout sellButtonContainer = view.findViewById(R.id.sell_button_container);
        final ConstraintLayout tryButtonContainer = view.findViewById(R.id.try_button_container);
        final ConstraintLayout positionContainer = view.findViewById(R.id.position_container);

        this.favIcon = view.findViewById(R.id.fav_icon);

        this.textViewSymbol = view.findViewById(R.id.stock_symbol);
        this.overlay = view.findViewById(R.id.screen_overlay);
        this.tradeButton = view.findViewById(R.id.trade_button);
        this.transactionList = view.findViewById(R.id.history_list);
        this.scrollView = view.findViewById(R.id.scroll_view);
        this.currentPrice = view.findViewById(R.id.current_price);
        this.priceChange = view.findViewById(R.id.change);
        this.companyName = view.findViewById(R.id.stock_name);
        this.open = view.findViewById(R.id.open);
        this.dailyLow = view.findViewById(R.id.daily_low);
        this.dailyHigh = view.findViewById(R.id.daily_high);
        this.yearlyLow = view.findViewById(R.id.yearly_low);
        this.yearlyHigh = view.findViewById(R.id.yearly_high);

        this.tradeButton.addToSpeedDial(buyButtonContainer);
        this.tradeButton.addToSpeedDial(sellButtonContainer);
        this.tradeButton.addToSpeedDial(tryButtonContainer);

        RecyclerView.LayoutManager transactionListManager = new LinearLayoutManager(getContext());
        this.transactionList.setLayoutManager(transactionListManager);

        this.symbol = getArguments().getString(getString(R.string.intent_extra_symbol));

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getMainActivity().setGlobalTitle(this.symbol);
        this.textViewSymbol.setText(this.symbol);
        this.transactionListAdapter =
                new TransactionViewAdapter(this.getFakeTransactionsForStock());
        this.transactionList.setAdapter(this.transactionListAdapter);

        CustomSparkView sparkView = view.findViewById(R.id.stock_chart);

        sparkView.setAdapter(dataAdapter);
        sparkView.setOnEndedCallback(
                () -> {
                    this.currentPrice.setText(Formatters.formatPrice(stockData.getCurrentPrice()));
                    this.priceChange.setText(this.generateChangeString());
                });
        sparkView.setScrubListener(
                value -> {
                    // disable scrolling when a value is selected
                    scrollView.requestDisallowInterceptTouchEvent(value != null);

                    if (value != null) {
                        Float price = (Float) value;

                        currentPrice.setText(Formatters.formatPrice(price));
                        this.priceChange.setText(this.generateChangeString(price));
                    }
                });

        this.tradeButton.setOnClickListener(v -> tradeButton.trigger(this.overlay));
        this.overlay.setOnClickListener(v -> tradeButton.close(v));

        if (!this.doesUserPositionExist()) {
            positionContainer.setVisibility(View.GONE);
        }

        this.tryButton = view.findViewById(R.id.try_button);
        this.tryButton.setOnClickListener(
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

        this.buyButton = this.tryButton = view.findViewById(R.id.buy_button);
        this.sellButton = this.tryButton = view.findViewById(R.id.sell_button);

        this.buyButton.setOnClickListener(
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
        this.sellButton.setOnClickListener(
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

        this.fetchInitialData();
    }

    private void fetchInitialData() {
        final Symbols symbols = new Symbols(Collections.singletonList(symbol));
        final MarketDataService marketDataService = new MarketDataService();

        Observable.zip(
                        marketDataService.getBars(AlpacaTimeframe.MINUTE, symbols),
                        marketDataService.getQuotes(symbols),
                        (bars, quotes) -> {
                            List<BarData> barData = bars.get(symbol);
                            QuoteData quoteData = quotes.get(symbol);

                            return new StockData(barData, quoteData);
                        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        stockData -> {
                            List<BarData> barData = stockData.getGraphData();
                            this.stockData = stockData;

                            this.loadStats();

                            dataAdapter.setData(
                                    barData.stream()
                                            .map(BarData::getClose)
                                            .collect(Collectors.toList()));
                            dataAdapter.setBaseline(barData.get(0).getOpen());
                            dataAdapter.notifyDataSetChanged();
                        },
                        err -> Log.e(TAG, err.toString()));
    }

    public static ArrayList<Pair<Float, Float>> getFakeStockPrices() {
        ArrayList<Pair<Float, Float>> list = new ArrayList<>();
        Float[] prices = Constants.stockDataPoints;

        for (int i = 0; i < prices.length; i += 5) {
            list.add(new Pair<>((float) i, prices[i]));
        }

        return list;
    }

    private void loadStats() {
        this.currentPrice.setText(Formatters.formatPrice(this.stockData.getCurrentPrice()));
        this.companyName.setText(this.stockData.getCompanyName());
        this.dailyLow.setText(Formatters.formatPrice(this.stockData.getLow()));
        this.dailyHigh.setText(Formatters.formatPrice(this.stockData.getHigh()));
        this.open.setText(Formatters.formatPrice(this.stockData.getOpen()));
        this.yearlyLow.setText(Formatters.formatPrice(this.stockData.getYearlyLow()));
        this.yearlyHigh.setText(Formatters.formatPrice(this.stockData.getYearlyHigh()));
        this.priceChange.setText(this.generateChangeString());
    }

    String generateChangeString() {
        float change = this.stockData.getCurrentPrice() - this.stockData.getOpen();
        float changePercentage = change * 100 / this.stockData.getOpen();
        String sign = change >= 0 ? "+" : "-";

        String formattedPrice = Formatters.formatPrice(Math.abs(change));

        return String.format(
                Locale.CANADA, "%s%s (%.2f)", sign, formattedPrice, Math.abs(changePercentage));
    }

    String generateChangeString(Float price) {
        float change = price - this.stockData.getOpen();
        float changePercentage = change * 100 / this.stockData.getOpen();
        String sign = change >= 0 ? "+" : "-";

        String formattedPrice = Formatters.formatPrice(Math.abs(change));

        return String.format(
                Locale.CANADA, "%s%s (%.2f)", sign, formattedPrice, Math.abs(changePercentage));
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
                                this.symbol,
                                100,
                                56.92f,
                                "buy",
                                LocalDateTime.of(2020, Month.AUGUST, 19, 13, 14))));
        list.add(
                new TransactionsListRow(
                        new Transaction(
                                this.symbol,
                                268,
                                36.47f,
                                "buy",
                                LocalDateTime.of(2020, Month.AUGUST, 1, 9, 52))));

        return list;
    }

    public static ArrayList<TransactionsListRow> getFakeTransactions() {
        ArrayList<TransactionsListRow> list = new ArrayList<>();

        list.add(new TransactionsListRow(LocalDateTime.of(2020, Month.AUGUST, 19, 13, 14)));
        list.add(
                new TransactionsListRow(
                        new Transaction(
                                "SHOP",
                                100,
                                56.92f,
                                "buy",
                                LocalDateTime.of(2020, Month.AUGUST, 19, 13, 14))));
        list.add(
                new TransactionsListRow(
                        new Transaction(
                                "SHOP",
                                268,
                                36.47f,
                                "buy",
                                LocalDateTime.of(2020, Month.AUGUST, 1, 9, 52))));

        return list;
    }
}
