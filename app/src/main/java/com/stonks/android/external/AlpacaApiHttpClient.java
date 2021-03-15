package com.stonks.android.external;

import com.stonks.android.BuildConfig;
import com.stonks.android.utility.Constants;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class AlpacaApiHttpClient {
    private static OkHttpClient httpClient;

    private AlpacaApiHttpClient() {}

    public static OkHttpClient getInstance() {
        if (httpClient == null) {
            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

            httpClientBuilder
                    .networkInterceptors()
                    .add(
                            chain -> {
                                Request.Builder requestBuilder = chain.request().newBuilder();

                                requestBuilder.addHeader(Constants.ALPACA_KEY_ID_HEADER, BuildConfig.ALPACA_KEY_ID);
                                requestBuilder.addHeader(Constants.ALPACA_SECRET_KEY_HEADER, BuildConfig.ALPACA_SECRET_KEY);

                                return chain.proceed(requestBuilder.build());
                            });

            return httpClientBuilder.build();
        }

        return httpClient;
    }
}
