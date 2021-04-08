package com.stonks.android.external;

import com.stonks.android.BuildConfig;
import com.stonks.android.model.BarData;
import com.stonks.android.model.QuoteData;
import com.stonks.android.model.Symbols;
import com.stonks.android.model.alpaca.AlpacaTimeframe;
import io.reactivex.rxjava3.core.Observable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MarketDataService {
    private final AlpacaMarketDataApi alpacaMarketDataApi;
    private final TdAmeritradeApi tdMarketDataApi;

    public MarketDataService() {
        Retrofit alpacaRetrofit =
                new Retrofit.Builder()
                        .baseUrl("https://data.alpaca.markets/")
                        .client(AlpacaApiHttpClient.getInstance())
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                        .build();

        Retrofit tdAmeritradeRetrofit =
                new Retrofit.Builder()
                        .baseUrl("https://api.tdameritrade.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                        .build();

        alpacaMarketDataApi = alpacaRetrofit.create(AlpacaMarketDataApi.class);
        tdMarketDataApi = tdAmeritradeRetrofit.create(TdAmeritradeApi.class);
    }

    public Observable<Map<String, List<BarData>>> getBars(
            Symbols symbols, AlpacaTimeframe timeframe, int limit) {
        HashMap<String, String> params = new HashMap<>();
        params.put("symbols", symbols.toString());
        params.put("limit", String.valueOf(limit));

        return alpacaMarketDataApi.getBars(timeframe, params);
    }

    public Observable<Map<String, List<BarData>>> getBars(
            Symbols symbols, AlpacaTimeframe timeframe, int limit, String start) {
        HashMap<String, String> params = new HashMap<>();
        params.put("symbols", symbols.toString());
        params.put("limit", String.valueOf(limit));
        params.put("start", start);

        return alpacaMarketDataApi.getBars(timeframe, params);
    }

    public Observable<Map<String, QuoteData>> getQuotes(Symbols symbols) {
        return tdMarketDataApi.getQuotes(BuildConfig.TD_API_KEY, symbols);
    }
}
