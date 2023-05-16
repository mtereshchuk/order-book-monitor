package com.mtereshchuk.assignment.provider.binance.impl;

import com.mtereshchuk.assignment.core.OrderBookManager;
import com.mtereshchuk.assignment.core.OrderBookStorage;
import com.mtereshchuk.assignment.provider.binance.BinanceRestClient;
import com.mtereshchuk.assignment.provider.binance.BinanceWebSocketClient;
import com.mtereshchuk.assignment.provider.binance.json.DepthEvent;
import com.mtereshchuk.assignment.provider.binance.json.OrderBook;

import java.math.BigDecimal;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

/**
 * @author mtereshchuk
 */
public class BinanceOrderBookManager implements OrderBookManager {
    private final BinanceRestClient restClient;
    private final BinanceWebSocketClient webSocketClient;

    private WebSocket webSocket;

    public BinanceOrderBookManager(BinanceRestClient restClient, BinanceWebSocketClient webSocketClient) {
        this.restClient = restClient;
        this.webSocketClient = webSocketClient;
    }

    @Override
    public CompletableFuture<Void> manage(OrderBookStorage orderBook) {
        var symbol = orderBook.symbol();

        var state = new AtomicReference<>(DepthState.STARTED);
        var webSocketCallback = new WebSocketCallback(orderBook, state);
        webSocket = webSocketClient.getDepthStream(symbol.toLowerCase(), webSocketCallback);

        var restOrderBook = restClient.getOrderBook(symbol.toUpperCase(), orderBook.maxCapacity());
        var bids = asMap(restOrderBook.bids());
        var asks = asMap(restOrderBook.asks());

        orderBook.batchUpdate(emptyList(), emptyList(), new OrderBookStorage.Entries(bids, asks));

        var lastUpdateId = restOrderBook.lastUpdateId();
        var readyFuture = new CompletableFuture<Void>();
        state.set(DepthState.REST_UPLOADED.withArtifacts(lastUpdateId, readyFuture));

        return readyFuture;
    }

    @Override
    public void close() {
        BinanceWebSocketClient.close(webSocket);
    }

    private static Map<BigDecimal, BigDecimal> asMap(List<OrderBook.Entry> entries) {
        return asMap(entries, entry -> true);
    }

    private static Map<BigDecimal, BigDecimal> asMap(List<OrderBook.Entry> entries, Predicate<OrderBook.Entry> predicate) {
        return entries.stream()
                .filter(predicate)
                .collect(toMap(OrderBook.Entry::price, OrderBook.Entry::size));
    }

    private static class WebSocketCallback implements BinanceWebSocketClient.Callback {
        private final OrderBookStorage orderBook;
        private final AtomicReference<DepthState> state;
        private final Queue<DepthEvent> pending; // used only by WebSocket thread
        private long lastUpdateId = -1; // used only by WebSocket thread

        public WebSocketCallback(OrderBookStorage orderBook, AtomicReference<DepthState> state) {
            this.orderBook = orderBook;
            this.state = state;
            this.pending = new ArrayDeque<>();
        }

        @Override
        public void onEvent(DepthEvent event) {
            switch (state.get()) { // state machine
                case STARTED:
                    pending.offer(event);
                    break;

                case REST_UPLOADED:
                    lastUpdateId = state.get().lastUpdateId;

                    DepthEvent curEvent;
                    while ((curEvent = pending.poll()) != null) {
                        updateIfNeeded(curEvent);
                    }

                    state.set(DepthState.WORKING.withArtifacts(-1, state.get().readyFuture));
                    // no break
                    // go ahead to WORKING

                case WORKING:
                    updateIfNeeded(event);

                    var readyFuture = state.get().readyFuture;
                    if (!readyFuture.isDone()) {
                        readyFuture.complete(null); // first time here
                    }
            }
        }

        @Override
        public void onError(Throwable error) {
            System.err.println("WebSocket connection failed");
            System.exit(1);
        }

        private void updateIfNeeded(DepthEvent event) {
            if (lastUpdateId < event.finalUpdateId()) {
                lastUpdateId = event.finalUpdateId();

                var bidsToRemove = getEntriesToRemove(event.bids());
                var asksToRemove = getEntriesToRemove(event.asks());
                var newBids = getNewEntries(event.bids());
                var newAsks = getNewEntries(event.asks());

                orderBook.batchUpdate(bidsToRemove, asksToRemove, new OrderBookStorage.Entries(newBids, newAsks));
            }
        }

        private List<BigDecimal> getEntriesToRemove(List<OrderBook.Entry> entries) {
            return entries.stream()
                    .filter(OrderBook.Entry.REMOVED_PREDICATE)
                    .map(OrderBook.Entry::price)
                    .toList();
        }

        private Map<BigDecimal, BigDecimal> getNewEntries(List<OrderBook.Entry> entries) {
            return asMap(entries, OrderBook.Entry.REMOVED_PREDICATE.negate());
        }
    }

    private enum DepthState {
        STARTED, REST_UPLOADED, WORKING;

        long lastUpdateId = -1;
        CompletableFuture<Void> readyFuture = null;

        DepthState withArtifacts(long lastUpdateId, CompletableFuture<Void> readyFuture) {
            this.lastUpdateId = lastUpdateId;
            this.readyFuture = readyFuture;
            return this;
        }
    }
}
