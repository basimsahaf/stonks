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
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class AlpacaWebSocketListener extends WebSocketListener {
    // using an AtomicBoolean instead of Boolean for thread safety
    private final AtomicBoolean authenticated;
    private final Gson gson;
    private final AlpacaWebSocket alpacaWebSocket;
    private final ExecutorService executorService;

    private static final String TAG = AlpacaWebSocketListener.class.getCanonicalName();
    private static final String THREAD_NAME = "AlpacaWebSocketListenerThread";

    public AlpacaWebSocketListener(AlpacaWebSocket alpacaWebSocket) {
        this.authenticated = new AtomicBoolean(false);
        this.gson = new Gson();
        this.alpacaWebSocket = alpacaWebSocket;
        this.executorService =
                Executors.newSingleThreadExecutor(
                        runnable -> new Thread(runnable, THREAD_NAME));
    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        Log.d(TAG, "WebSocket open");

        if (!authenticated.get()) {
            webSocket.send(WebSocketRequest.getHandshakeAuthMessage());
        }
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
        Log.d(TAG, "WebSocket failure");
    }

    @Override
    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        Log.d(TAG, "WebSocket Closing: " + reason);
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        Log.d(TAG, "WebSocket Closed: " + reason);
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
            Log.d(TAG, "WebSocket stream is unauthenticated");
            if (message.getStream().equals("authorization")) {
                if (message.getData().getStatus().equals("authorized")) {
                    Log.i(TAG, "WebSocket stream is authenticated");
                    this.authenticated.set(true);
                } else {
                    Log.i(TAG, "WebSocket authorization failed");
                    // TODO: re-authenticate
                }
            }
        } else {
            if ("listening".equals(message.getStream())) {
                // received a subscription confirmation
                // move observers from pending map to confirmed map
                message.getData()
                        .getStreams()
                        .stream()
                        .map(this::removeAlpacaPrefix)
                        .forEach(alpacaWebSocket::confirmSubscription);
            } else {
                // received a new price update
                this.alpacaWebSocket.updateCurrentPrice(removeAlpacaPrefix(message.getStream()), message.getData().getClose());
            }
        }
    }

    private String removeAlpacaPrefix(String streamName) {
        return streamName.replace("AM.", "");
    }
}
