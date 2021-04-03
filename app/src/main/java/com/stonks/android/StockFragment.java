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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stonks.android.adapter.StockChartAdapter;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.databinding.FragmentStockBinding;
import com.stonks.android.external.MarketDataService;
import com.stonks.android.model.*;
import com.stonks.android.model.alpaca.AlpacaTimeframe;
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
    private TextView currentPrice, priceChange;
    private SpeedDialExtendedFab tradeButton;
    private LinearLayout overlay;
    private NestedScrollView scrollView;
    private ImageView changeIndicator;
    private final StockChartAdapter dataAdapter = new StockChartAdapter(new ArrayList<>());
    private ImageView favIcon;

    private boolean favourited = false;
    private final StockData stockData = new StockData();

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

        this.tradeButton.addToSpeedDial(buyButtonContainer);
        this.tradeButton.addToSpeedDial(sellButtonContainer);
        this.tradeButton.addToSpeedDial(tryButtonContainer);

        RecyclerView.LayoutManager transactionListManager = new LinearLayoutManager(getContext());
        this.transactionList.setLayoutManager(transactionListManager);
        this.transactionListAdapter =
                new TransactionViewAdapter(this.getFakeTransactionsForStock());
        this.transactionList.setAdapter(this.transactionListAdapter);

        CustomSparkView sparkView = view.findViewById(R.id.stock_chart);

        sparkView.setAdapter(dataAdapter);
        sparkView.setOnEndedCallback(
                () -> {
                    this.currentPrice.setText(Formatters.formatPrice(stockData.getCurrentPrice()));
                    this.priceChange.setText(this.generateChangeString());
                    this.changeIndicator.setImageDrawable(getIndicatorDrawable());
                });
        sparkView.setScrubListener(
                value -> {
                    // disable scrolling when a value is selected
                    scrollView.requestDisallowInterceptTouchEvent(value != null);

                    if (value != null) {
                        Float price = (Float) value;

                        currentPrice.setText(Formatters.formatPrice(price));
                        this.priceChange.setText(this.generateChangeString(price));
                        this.changeIndicator.setImageDrawable(getIndicatorDrawable(price));
                    }
                });

        this.tradeButton.setOnClickListener(v -> tradeButton.trigger(this.overlay));
        this.overlay.setOnClickListener(v -> tradeButton.close(v));

        if (!this.doesUserPositionExist()) {
            positionContainer.setVisibility(View.GONE);
        }

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
                        newStockData -> {
                            List<BarData> barData = newStockData.getGraphData();
                            this.stockData.updateStock(newStockData);

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
                new ForegroundColorSpan(ContextCompat.getColor(getContext(), color)),
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
        Log.d(TAG, "change: " + (change));
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
