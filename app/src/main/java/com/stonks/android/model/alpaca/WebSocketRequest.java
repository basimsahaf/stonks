package com.stonks.android.model.alpaca;

import android.util.Log;
import com.stonks.android.BuildConfig;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WebSocketRequest {
    public static String TAG = WebSocketRequest.class.getCanonicalName();

    public static String getAMStreamRequest(List<String> symbols) {
        List<String> quotes = symbols.stream().map(sym -> "AM." + sym).collect(Collectors.toList());

        try {
            return new JSONObject()
                    .put("action", "listen")
                    .put("data", new JSONObject().put("streams", new JSONArray(quotes)))
                    .toString();
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return "";
        }
    }

    public static String getHandshakeAuthMessage() {
        try {
            return new JSONObject()
                    .put("action", "authenticate")
                    .put(
                            "data",
                            new JSONObject()
                                    .put("key_id", BuildConfig.ALPACA_KEY_ID)
                                    .put("secret_key", BuildConfig.ALPACA_SECRET_KEY))
                    .toString();
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return "";
        }
    }
}
