package com.mtereshchuk.assignment.core.impl;

import com.mtereshchuk.assignment.core.OrderBookStorage;

import java.math.BigDecimal;
import java.util.*;

import static com.mtereshchuk.assignment.utils.CollectionUtils.immutableCopyOf;

/**
 * Thread-safe in memory order book storage implementation
 *
 * @author mtereshchuk
 */
public class InMemoryOrderBook implements OrderBookStorage {
    private static final int MAX_CAPACITY_MULTIPLIER = 10;

    private final String symbol;
    private final int maxCapacity;
    private final NavigableMap<BigDecimal, BigDecimal> bids;
    private final NavigableMap<BigDecimal, BigDecimal> asks;
    private final Object lock;

    public InMemoryOrderBook(String symbol, int limit) {
        this.symbol = symbol;
        this.maxCapacity = limit * MAX_CAPACITY_MULTIPLIER;
        this.bids = new TreeMap<>(BIDS_COMPARATOR);
        this.asks = new TreeMap<>(ASKS_COMPARATOR);
        this.lock = new Object();
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public int maxCapacity() {
        return maxCapacity;
    }

    @Override
    public Entries createSnapshot(int limit) {
        synchronized (lock) {
            return new Entries(
                    immutableCopyOf(bids, limit),
                    immutableCopyOf(asks, limit));
        }
    }

    @Override
    public void batchUpdate(List<BigDecimal> bidsToRemove, List<BigDecimal> asksToRemove, Entries newEntries) {
        synchronized (lock) {
            bidsToRemove.forEach(bids::remove);
            asksToRemove.forEach(asks::remove);

            putIfFitOrReplaceAll(bids, newEntries.bids());
            putIfFitOrReplaceAll(asks, newEntries.asks());
        }
    }

    private void putIfFitOrReplaceAll(NavigableMap<BigDecimal, BigDecimal> entryMap, Map<BigDecimal, BigDecimal> content) {
        content.forEach((price, size) -> putIfFitOrReplace(entryMap, price, size));
    }

    private void putIfFitOrReplace(NavigableMap<BigDecimal, BigDecimal> entryMap, BigDecimal price, BigDecimal size) {
        if (entryMap.size() == maxCapacity) {
            var lastPrice = entryMap.lastEntry().getKey();
            if (entryMap.comparator().compare(price, lastPrice) > 0) {
                return;
            }
            entryMap.pollLastEntry();
        }
        entryMap.put(price, size);
    }
}
