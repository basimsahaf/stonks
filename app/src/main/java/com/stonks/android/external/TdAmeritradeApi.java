package com.stonks.android.external;

import com.stonks.android.model.QuoteData;
import com.stonks.android.model.Symbols;
import io.reactivex.rxjava3.core.Observable;
import java.util.Map;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TdAmeritradeApi {
    @GET("/v1/marketdata/quotes")
    Observable<Map<String, QuoteData>> getQuotes(
            @Query("apikey") String apiKey, @Query("symbol") Symbols symbols);
}
