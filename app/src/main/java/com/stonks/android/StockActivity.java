package com.stonks.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.stonks.android.adapter.StockChartAdapter;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.external.MarketDataService;
import com.stonks.android.model.*;
import com.stonks.android.uicomponent.CustomSparkView;
import com.stonks.android.uicomponent.SpeedDialExtendedFab;
import com.stonks.android.utility.Constants;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class StockActivity extends BaseActivity {
  private static String TAG = StockActivity.class.getCanonicalName();

  private String symbol;
  private RecyclerView transactionList;
  private RecyclerView
      .Adapter<TransactionViewAdapter.ViewHolder> transactionListAdapter;
  private ConstraintLayout buyButtonContainer, sellButtonContainer,
      tryButtonContainer;
  private TextView textViewSymbol;
  private SpeedDialExtendedFab tradeButton;
  private LinearLayout overlay;
  private NestedScrollView scrollView;
  private final StockChartAdapter dataAdapter =
      new StockChartAdapter(new ArrayList<>());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_stock);

    this.textViewSymbol = findViewById(R.id.stock_symbol);
    this.overlay = findViewById(R.id.screen_overlay);
    this.tradeButton = findViewById(R.id.trade_button);
    this.transactionList = findViewById(R.id.history_list);
    this.buyButtonContainer = findViewById(R.id.buy_button_container);
    this.sellButtonContainer = findViewById(R.id.sell_button_container);
    this.tryButtonContainer = findViewById(R.id.try_button_container);
    this.scrollView = findViewById(R.id.scroll_view);
    TextView currentPrice = findViewById(R.id.current_price);

    this.tradeButton.addToSpeedDial(this.buyButtonContainer);
    this.tradeButton.addToSpeedDial(this.sellButtonContainer);
    this.tradeButton.addToSpeedDial(this.tryButtonContainer);

    RecyclerView.LayoutManager transactionListManager =
        new LinearLayoutManager(this);
    this.transactionList.setLayoutManager(transactionListManager);
    this.transactionListAdapter =
        new TransactionViewAdapter(this.getFakeTransactions());
    this.transactionList.setAdapter(this.transactionListAdapter);

    Intent intent = getIntent();
    this.symbol =
        intent.getStringExtra(getString(R.string.intent_extra_symbol));
    getSupportActionBar().setDisplayShowTitleEnabled(true);
    getSupportActionBar().setTitle(this.symbol);

    this.textViewSymbol.setText(this.symbol);

    CustomSparkView sparkView = findViewById(R.id.stock_chart);

    dataAdapter.setData(this.getStockPrices());
    dataAdapter.setBaseline(121.08f);

    sparkView.setAdapter(dataAdapter);
    sparkView.setScrubListener(value -> {
      // disable scrolling when a value is selected
      scrollView.requestDisallowInterceptTouchEvent(value != null);

      if (value != null) {
        currentPrice.setText(String.format(Locale.CANADA, "$%.2f", value));
      }
    });

    this.tradeButton.setOnClickListener(v -> tradeButton.trigger(this.overlay));
    this.overlay.setOnClickListener(v -> tradeButton.close(v));

    this.fetchInitialData();
  }

  @Override
  public void onBackPressed() {
    if (this.tradeButton.isExtended()) {
      super.onBackPressed();
    } else {
      this.tradeButton.close(this.overlay);
    }
  }

  private void fetchInitialData() {
    final TextView currentPrice = findViewById(R.id.current_price);
    final TextView open = findViewById(R.id.open);
    final TextView dailyLow = findViewById(R.id.daily_low);
    final TextView dailyHigh = findViewById(R.id.daily_high);
    final Symbols symbols = new Symbols(Collections.singletonList(symbol));
    final MarketDataService marketDataService = new MarketDataService();

    Observable
        .zip(marketDataService.getBars(AlpacaTimeframe.MINUTE, symbols),
             marketDataService.getQuotes(symbols),
             (bars, quotes) -> {
               List<BarData> barData = bars.get(symbol);
               QuoteData quoteData = quotes.get(symbol);

               return new StockData(barData, quoteData);
             })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(stockData -> {
          List<BarData> barData = stockData.getGraphData();

          dailyLow.setText(
              String.format(Locale.CANADA, "$%.2f", stockData.getLow()));
          dailyHigh.setText(
              String.format(Locale.CANADA, "$%.2f", stockData.getHigh()));
          open.setText(
              String.format(Locale.CANADA, "$%.2f", stockData.getOpen()));
          currentPrice.setText(String.format(Locale.CANADA, "$%.2f",
                                             stockData.getCurrentPrice()));

          dataAdapter.setData(barData.stream()
                                  .map(BarData::getClose)
                                  .collect(Collectors.toList()));
          dataAdapter.setBaseline(barData.get(0).getOpen());
          dataAdapter.notifyDataSetChanged();
        }, err -> Log.e(TAG, err.toString()));
  }

  public static ArrayList<Pair<Float, Float>> getFakeStockPrices() {
    ArrayList<Pair<Float, Float>> list = new ArrayList<>();
    Float[] prices = Constants.stockDataPoints;

    for (int i = 0; i < prices.length; i += 5) {
      list.add(new Pair<>((float)i, prices[i]));
    }

    return list;
  }

  private List<Float> getStockPrices() {
    return Arrays.asList(Constants.stockDataPoints);
  }

  public static ArrayList<Transaction> getFakeTransactions() {
    ArrayList<Transaction> list = new ArrayList<>();

    list.add(new Transaction("UBER", 100, 56.92f, "buy",
                             LocalDateTime.of(2020, Month.AUGUST, 19, 13, 14)));
    list.add(new Transaction("UBER", 268, 36.47f, "buy",
                             LocalDateTime.of(2020, Month.AUGUST, 1, 9, 52)));

    return list;
  }
}
