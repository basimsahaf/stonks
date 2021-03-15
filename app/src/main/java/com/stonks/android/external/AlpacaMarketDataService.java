package com.stonks.android.external;

import com.stonks.android.model.BarData;
import io.reactivex.rxjava3.core.Observable;
import java.util.List;
import java.util.Map;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AlpacaMarketDataService {
    private final AlpacaMarketDataApi marketDataApi;

    public AlpacaMarketDataService() {
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl("https://data.alpaca.markets/")
                        .client(AlpacaApiHttpClient.getInstance())
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                        .build();

        marketDataApi = retrofit.create(AlpacaMarketDataApi.class);
    }

    public Observable<Map<String, List<BarData>>> getBars(
            AlpacaMarketDataApi.AlpacaTimeframe timeframe, AlpacaMarketDataApi.Symbols symbols) {
        return marketDataApi.getBars(timeframe, symbols);
    }
}
