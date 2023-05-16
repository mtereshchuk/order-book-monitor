package com.mtereshchuk.assignment.core;

import java.math.BigDecimal;
import java.util.*;

import static com.mtereshchuk.assignment.utils.CollectionUtils.immutableCopyOf;

/**
 * @author mtereshchuk
 */
public class TestOrderBook implements OrderBookStorage {
    private final String symbol;
    private final SortedMap<BigDecimal, BigDecimal> bids;
    private final SortedMap<BigDecimal, BigDecimal> asks;

    public TestOrderBook(String symbol) {
        this.symbol = symbol;
        this.bids = new TreeMap<>(BIDS_COMPARATOR);
        this.asks = new TreeMap<>(ASKS_COMPARATOR);
    }

    public static TestOrderBook of(String symbol, Map<Double, Double> bids, Map<Double, Double> asks) {
        var orderBook = new TestOrderBook(symbol);
        putAll(orderBook.bids, bids);
        putAll(orderBook.asks, asks);
        return orderBook;
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public int maxCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Entries createSnapshot(int limit) {
        return new Entries(
                immutableCopyOf(bids, limit),
                immutableCopyOf(asks, limit));
    }

    @Override
    public void batchUpdate(List<BigDecimal> bidsToRemove, List<BigDecimal> asksToRemove, Entries newEntries) {
        bidsToRemove.forEach(bids::remove);
        asksToRemove.forEach(asks::remove);

        bids.putAll(newEntries.bids());
        asks.putAll(newEntries.asks());
    }

    private static void putAll(Map<BigDecimal, BigDecimal> entryMap, Map<Double, Double> content) {
        content.forEach((price, size) ->
                entryMap.put(BigDecimal.valueOf(price), BigDecimal.valueOf(size)));
    }
}
