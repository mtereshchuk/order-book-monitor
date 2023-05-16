package com.mtereshchuk.assignment.provider.binance.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtereshchuk.assignment.provider.binance.BinanceApiConfig;
import com.mtereshchuk.assignment.provider.binance.BinanceApiException;
import com.mtereshchuk.assignment.provider.binance.BinanceWebSocketClient;
import com.mtereshchuk.assignment.provider.binance.json.DepthEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

/**
 * @author mtereshchuk
 */
public class BinanceWebSocketClientImpl implements BinanceWebSocketClient {
    private static final String DEPTH_URL_FORMATTER = BinanceApiConfig.WEB_SOCKET_URL + "%s@depth@100ms";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BinanceWebSocketClientImpl(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public WebSocket getDepthStream(String symbol, Callback callback) {
        var uri = URI.create(String.format(DEPTH_URL_FORMATTER, symbol));
        var listener = new DepthListener(objectMapper, callback);
        return httpClient.newWebSocketBuilder().buildAsync(uri, listener).join();
    }

    private static class DepthListener implements WebSocket.Listener {
        private final ObjectMapper objectMapper;
        private final Callback callback;

        public DepthListener(ObjectMapper objectMapper, Callback callback) {
            this.objectMapper = objectMapper;
            this.callback = callback;
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            try {
                var depthEvent = objectMapper.readValue(data.toString(), DepthEvent.class);
                callback.onEvent(depthEvent);
            } catch (IOException e) {
                throw new BinanceApiException(e);
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            callback.onError(error);
        }
    }
}
