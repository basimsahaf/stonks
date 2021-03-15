package com.stonks.android.external;

import com.stonks.android.model.BarData;
import io.reactivex.rxjava3.core.Observable;
import java.util.List;
import java.util.Map;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AlpacaMarketDataApi {
    @GET("/v1/bars/{timeframe}")
    Observable<Map<String, List<BarData>>> getBars(
            @Path("timeframe") AlpacaTimeframe timeframe, @Query("symbols") Symbols symbols);

    class Symbols {
        private final List<String> symbols;

        public Symbols(List<String> symbols) {
            this.symbols = symbols;
        }

        @Override
        public String toString() {
            return String.join(",", this.symbols);
        }
    }

    enum AlpacaTimeframe {
        MINUTE("minute"),
        MINUTES_5("5min"),
        MINUTES_15("15min"),
        DAY("day");

        private final String timeframe;

        AlpacaTimeframe(final String timeframe) {
            this.timeframe = timeframe;
        }

        @Override
        public String toString() {
            return this.timeframe;
        }
    }
}
