package com.mtereshchuk.assignment.provider.binance;

import com.mtereshchuk.assignment.provider.binance.json.DepthEvent;

import java.net.http.WebSocket;

/**
 * Small subset of Binance WebSocket API methods for getting order books
 *
 * @author mtereshchuk
 * @see <a href="https://github.com/binance/binance-spot-api-docs/blob/master/web-socket-streams.md#diff-depth-stream>Binance WebSocket API</a>
 */
public interface BinanceWebSocketClient {
    /**
     * @param symbol   ticker symbol (e.g. ETHUSDT)
     * @param callback callback for asynchronous actions
     */
    WebSocket getDepthStream(String symbol, Callback callback);

    /**
     * Closes the provided WebSocket instance
     */
    static void close(WebSocket webSocket) {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "").join();
    }

    @FunctionalInterface
    interface Callback {
        /**
         * On diff depth event
         */
        void onEvent(DepthEvent event);

        /**
         * On WebSocket error
         */
        default void onError(Throwable error) {
        }
    }
}
