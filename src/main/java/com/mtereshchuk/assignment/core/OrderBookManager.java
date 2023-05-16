package com.mtereshchuk.assignment.core;

import java.util.concurrent.CompletableFuture;

/**
 * Order book manager
 *
 * @author mtereshchuk
 */
public interface OrderBookManager extends AutoCloseable {
    /**
     * Manages the order book in accordance with the specifics of the related provider
     *
     * @param orderBook shared order book storage
     * @return future indicates order book was filled valid at least once
     */
    CompletableFuture<Void> manage(OrderBookStorage orderBook);
}
