package com.mtereshchuk.assignment.core;

/**
 * Provider for order book manager
 *
 * @author mtereshchuk
 * @see OrderBookManager
 * @see java.util.ServiceLoader
 */
public interface BookManagerProvider {
    /**
     * Identical name of the current provider
     */
    String name();

    /**
     * Creates new order book manager
     */
    OrderBookManager provide();
}
