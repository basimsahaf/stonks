package com.stonks.android.external;

import com.stonks.android.model.BarData;
import com.stonks.android.model.alpaca.AlpacaTimeframe;
import io.reactivex.rxjava3.core.Observable;
import java.util.List;
import java.util.Map;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface AlpacaMarketDataApi {
    @GET("/v1/bars/{timeframe}")
    Observable<Map<String, List<BarData>>> getBars(
            @Path("timeframe") AlpacaTimeframe timeframe, @QueryMap Map<String, String> params);
}
