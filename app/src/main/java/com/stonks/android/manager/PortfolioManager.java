package com.stonks.android.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.stonks.android.HomePageFragment;
import com.stonks.android.R;
import com.stonks.android.external.MarketDataService;
import com.stonks.android.model.BarData;
import com.stonks.android.model.Portfolio;
import com.stonks.android.model.PortfolioItem;
import com.stonks.android.model.QuoteData;
import com.stonks.android.model.StockData;
import com.stonks.android.model.StockListItem;
import com.stonks.android.model.Symbols;
import com.stonks.android.model.alpaca.AlpacaTimeframe;
import com.stonks.android.storage.PortfolioTable;
import com.stonks.android.storage.UserTable;
import com.stonks.android.utility.Formatters;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PortfolioManager {
    private static PortfolioManager portfolioManager = null;

    private static UserTable userTable;
    private static PortfolioTable portfolioTable;
    private static Portfolio portfolio;
    private static MarketDataService marketDataService;
    private static ArrayList<StockListItem> stocksList;

    private PortfolioManager(Context context) {
        userTable = new UserTable(context);
        portfolioTable = new PortfolioTable(context);
        marketDataService = new MarketDataService();
        stocksList = new ArrayList<>();
    }

    public static PortfolioManager getInstance(Context context) {
        if (portfolioManager == null) {
            portfolioManager = new PortfolioManager(context);

            //String username = LoginRepository.getInstance(new LoginDataSource(userTable)).getCurrentUser();
            portfolio = new Portfolio(0.0f, 0.0f, portfolioTable.getPortfolioItems("username"));

            ArrayList<String> symbolList = new ArrayList<>();
            for (PortfolioItem item : portfolio.getPortfolioItems()) {
                symbolList.add(item.getSymbol());
            }
            symbolList.add("SHOP");
            symbolList.add("UBER");

            Symbols symbols = new Symbols(symbolList);
            marketDataService.getBars(AlpacaTimeframe.MINUTE, symbols)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            map -> {
                                stocksList.clear();

                                for(String symbol: symbolList) {
                                    List<BarData> barData = map.get(symbol);
                                    float currentPrice = barData.get(barData.size() - 1).getClose();
                                    float change = currentPrice - barData.get(0).getOpen();
                                    float changePercentage = change * 100 / barData.get(0).getOpen();

                                    stocksList.add(new StockListItem(symbol, currentPrice, 2, change, changePercentage));
                                }

                                HomePageFragment.updateData();
                            },
                            err -> Log.e("PortfolioManager", err.toString()));
        }

        return portfolioManager;
    }

    public float getAccountBalance() {
        return portfolio.getAccountBalance();
    }

    public float getAccountValue() {
        return portfolio.getAccountValue();
    }

    public ArrayList<StockListItem> getStocks() {
        ArrayList<StockListItem> list = new ArrayList<>();

        for (PortfolioItem p : portfolio.getPortfolioItems()) {
            list.add(new StockListItem(p.getSymbol(), 100.0f, p.getQuantity(), 7.0f, 3.4f));
        }

        if (list.isEmpty()) {
            list.add(new StockListItem("SHOP", 100.0f, 2, 7.0f, 7.0f));
            list.add(new StockListItem("UBER", 100.0f, 2, 7.0f, 7.0f));
        }

        return stocksList;
    }
}
