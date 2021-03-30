package com.stonks.android.external;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.stonks.android.model.alpaca.WebSocketRequest;
import com.stonks.android.model.alpaca.WebSocketResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class AlpacaWebSocketListener extends WebSocketListener {
    private final AtomicBoolean authenticated;
    private final Gson gson;
    private final BiConsumer<String, Float> updateCurrentPrice;
    private final Consumer<String> confirmSubscription;
    private static final String TAG = AlpacaWebSocketListener.class.getCanonicalName();
    private final ExecutorService executorService;

    public AlpacaWebSocketListener(
            BiConsumer<String, Float> updateCurrentPrice, Consumer<String> confirmSubscription) {
        this.authenticated = new AtomicBoolean(false);
        this.gson = new Gson();
        this.updateCurrentPrice = updateCurrentPrice;
        this.confirmSubscription = confirmSubscription;
        this.executorService =
                Executors.newSingleThreadExecutor(
                        runnable -> new Thread(runnable, "AlpacaWebSocketListenerThread"));
    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        Log.d(TAG, "websocket open");
        if (!authenticated.get()) {
            webSocket.send(WebSocketRequest.getHandshakeAuthMessage());
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        Log.d(TAG, "WebSocket failure");
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "WebSocket Closing");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "WebSocket Closed");
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        executorService.execute(() -> this.handleMessage(text, authenticated));
    }

    private void handleMessage(String text, AtomicBoolean authenticated) {
        WebSocketResponse message = gson.fromJson(text, WebSocketResponse.class);

        Log.d(TAG, "Handling: " + text);
        Log.d(TAG, message.toString());

        if (!authenticated.get()) {
            Log.d(TAG, "unauthenticated");
            if (message.getStream().equals("authorization")) {
                Log.d(TAG, message.getData().toString());
                Log.d(TAG, message.getData().getStatus());
                if (message.getData().getStatus().equals("authorized")) {
                    Log.i(TAG, "Socket stream authenticated");
                    this.authenticated.set(true);
                } else {
                    Log.i(TAG, "websocket authorization failed");
                    // TODO: re-authenticate
                }
            }
        } else {
            final String stream = message.getStream();

            Log.d(TAG, "Stream: " + stream);

            if ("listening".equals(stream)) {
                message.getData()
                        .getStreams()
                        .forEach(
                                rawSymbol ->
                                        this.confirmSubscription.accept(
                                                rawSymbol.replace("AM.", "")));
            } else {
                Log.d(TAG, "new price: " + message.getData().getClose());
                this.updateCurrentPrice.accept(
                        message.getStream().replace("AM.", ""), message.getData().getClose());
            }
        }
    }
}
