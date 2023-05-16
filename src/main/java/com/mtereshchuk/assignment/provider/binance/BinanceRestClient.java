package com.mtereshchuk.assignment.provider.binance;

import com.mtereshchuk.assignment.provider.binance.json.OrderBook;

/**
 * Small subset of Binance REST API methods for getting order books
 *
 * @author mtereshchuk
 * @see <a href="https://github.com/binance/binance-spot-api-docs/blob/master/rest-api.md#order-book>Binance REST API</a>
 */
public interface BinanceRestClient {
    /**
     * Gets order book synchronously
     *
     * @param symbol ticker symbol (e.g. ETHUSDT)
     * @param limit  depth of the order book
     */
    OrderBook getOrderBook(String symbol, int limit);
}
