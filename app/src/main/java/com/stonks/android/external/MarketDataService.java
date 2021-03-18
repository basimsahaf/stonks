package com.stonks.android.external;

import com.stonks.android.BuildConfig;
import com.stonks.android.model.AlpacaTimeframe;
import com.stonks.android.model.BarData;
import com.stonks.android.model.QuoteData;
import com.stonks.android.model.Symbols;
import io.reactivex.rxjava3.core.Observable;
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
            AlpacaTimeframe timeframe, Symbols symbols) {
        return alpacaMarketDataApi.getBars(timeframe, symbols);
    }

    public Observable<Map<String, QuoteData>> getQuotes(Symbols symbols) {
        return tdMarketDataApi.getQuotes(BuildConfig.TD_API_KEY, symbols);
    }
}
