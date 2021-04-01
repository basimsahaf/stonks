package com.stonks.android.external;

import com.stonks.android.model.WebSocketObserver;
import com.stonks.android.model.alpaca.WebSocketRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class AlpacaWebSocket {
    private final WebSocket socket;
    private final Map<String, List<WebSocketObserver>> subscribedSymbols;
    private final Map<String, List<WebSocketObserver>> pendingSubscriptions;

    public AlpacaWebSocket() {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.DAYS).build();
        Request request = new Request.Builder().url("wss://data.alpaca.markets/stream").build();

        this.subscribedSymbols = new HashMap<>();
        this.pendingSubscriptions = new HashMap<>();

        AlpacaWebSocketListener listener = new AlpacaWebSocketListener(this);
        this.socket = client.newWebSocket(request, listener);
    }

    public void subscribe(String symbol, WebSocketObserver observer) {
        this.pendingSubscriptions.put(symbol, Collections.singletonList(observer));
        this.socket.send(
                WebSocketRequest.getAMStreamRequest(
                        new ArrayList<>(Collections.singletonList(symbol))));
    }

    public void updateCurrentPrice(String symbol, Float price) {
        subscribedSymbols
                .getOrDefault(symbol, new ArrayList<>())
                .forEach(observer -> observer.updateCurrentPrice(price));
    }

    public void confirmSubscription(String symbol) {
        List<WebSocketObserver> observers =
                pendingSubscriptions.getOrDefault(symbol, new ArrayList<>());

        if (subscribedSymbols.containsKey(symbol)) {
            subscribedSymbols.get(symbol).addAll(observers);
        } else {
            subscribedSymbols.put(symbol, new ArrayList<>(observers));
        }
    }
}
