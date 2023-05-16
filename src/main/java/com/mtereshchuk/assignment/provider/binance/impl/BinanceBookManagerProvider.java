package com.mtereshchuk.assignment.provider.binance.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtereshchuk.assignment.core.BookManagerProvider;
import com.mtereshchuk.assignment.core.OrderBookManager;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;

import static com.mtereshchuk.assignment.utils.ThreadUtils.namedThread;

/**
 * @author mtereshchuk
 */
public class BinanceBookManagerProvider implements BookManagerProvider {
    public static final String NAME = "binance";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public OrderBookManager provide() {
        var httpClient = HttpClient.newBuilder()
                .executor(Executors.newSingleThreadExecutor(namedThread("binance-http-thread")))
                .build();
        var objectMapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // api data can be extended

        var restClient = new BinanceRestClientImpl(httpClient, objectMapper);
        var webSocketClient = new BinanceWebSocketClientImpl(httpClient, objectMapper);

        return new BinanceOrderBookManager(restClient, webSocketClient);
    }
}
