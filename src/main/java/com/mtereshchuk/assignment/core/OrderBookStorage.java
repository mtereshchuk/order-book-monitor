package com.mtereshchuk.assignment.core;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Shared order book storage
 *
 * @author mtereshchuk
 */
public interface OrderBookStorage {
    Comparator<BigDecimal> BIDS_COMPARATOR = Comparator.reverseOrder();
    Comparator<BigDecimal> ASKS_COMPARATOR = Comparator.naturalOrder();

    /**
     * Ticker symbol
     */
    String symbol();

    /**
     * Maximum storage capacity. If the max capacity exceeded, then last entry will be removed
     */
    int maxCapacity();

    /**
     * Creates a current snapshot of the order book
     *
     * @param limit depth of the order book
     */
    Entries createSnapshot(int limit);

    /**
     * Creates a full snapshot of the order book
     */
    default Entries createSnapshot() {
        return createSnapshot(maxCapacity());
    }

    /**
     * Do mass update by one action
     *
     * @param bidsToRemove to be removed bid prices
     * @param asksToRemove to be removed ask prices
     * @param newEntries   new entries to be added
     */
    void batchUpdate(List<BigDecimal> bidsToRemove, List<BigDecimal> asksToRemove, Entries newEntries);

    /**
     * Data class for sharing bids and asks
     */
    record Entries(Map<BigDecimal, BigDecimal> bids, Map<BigDecimal, BigDecimal> asks) {
    }
}
