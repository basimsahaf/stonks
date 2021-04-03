package com.stonks.android.model.alpaca;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * From
 * https://alpaca.markets/docs/api-documentation/api-v2/market-data/alpaca-data-api-v1/streaming/
 *
 * <p>Used by GSON to parse JSON into an object
 */
public class WebSocketResponse {
    private String stream;
    private WebSocketData data;

    public String getStream() {
        return stream;
    }

    public WebSocketData getData() {
        return data;
    }

    public static final class WebSocketData {
        private String action;
        private String status;
        private String error;
        private List<String> streams;

        @SerializedName("ev")
        private String eventName;

        @SerializedName("T")
        private String symbol;

        @SerializedName("v")
        private int volume;

        @SerializedName("av")
        private int accumulatedVolume;

        @SerializedName("op")
        private float officialOpen;

        @SerializedName("vw")
        private float vwap;

        @SerializedName("o")
        private float open;

        @SerializedName("c")
        private float close;

        @SerializedName("h")
        private float high;

        @SerializedName("l")
        private float low;

        @SerializedName("a")
        private float averagePrice;

        @SerializedName("s")
        private long startTimestamp;

        @SerializedName("e")
        private long endTimestamp;

        public String getEventName() {
            return eventName;
        }

        public String getSymbol() {
            return symbol;
        }

        public int getVolume() {
            return volume;
        }

        public int getAccumulatedVolume() {
            return accumulatedVolume;
        }

        public float getOfficialOpen() {
            return officialOpen;
        }

        public float getVwap() {
            return vwap;
        }

        public float getOpen() {
            return open;
        }

        public float getClose() {
            return close;
        }

        public float getHigh() {
            return high;
        }

        public float getLow() {
            return low;
        }

        public float getAveragePrice() {
            return averagePrice;
        }

        public long getStartTimestamp() {
            return startTimestamp;
        }

        public long getEndTimestamp() {
            return endTimestamp;
        }

        public String getAction() {
            return action;
        }

        public String getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public List<String> getStreams() {
            return streams;
        }
    }
}
