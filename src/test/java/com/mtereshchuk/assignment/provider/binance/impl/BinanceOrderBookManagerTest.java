package com.mtereshchuk.assignment.provider.binance.impl;

import com.mtereshchuk.assignment.core.TestOrderBook;
import com.mtereshchuk.assignment.provider.binance.BinanceRestClient;
import com.mtereshchuk.assignment.provider.binance.BinanceWebSocketClient;
import com.mtereshchuk.assignment.provider.binance.BinanceWebSocketClient.Callback;
import com.mtereshchuk.assignment.provider.binance.json.OrderBook;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.net.http.WebSocket;
import java.util.List;

import static com.mtereshchuk.assignment.utils.TestUtils.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author mtereshchuk
 */
public class BinanceOrderBookManagerTest {
    @Test
    public void manageTest() {
        var symbol = "ETHUSDT";
        var symbolLower = symbol.toLowerCase();

        var webSocketClient = mock(BinanceWebSocketClient.class);
        doAnswer((Answer<WebSocket>) invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onEvent(eventOf(symbol, 1, entryOf(1, 1), entryOf(10, 10)));
            callback.onEvent(eventOf(symbol, 2, entryOf(2, 2), entryOf(20, 20)));
            return spy(WebSocket.class);
        }).when(webSocketClient).getDepthStream(eq(symbolLower), any(Callback.class));

        var restClient = mock(BinanceRestClient.class);
        var restOrderBook = new OrderBook(3,
                List.of(entryOf(3, 3)),
                List.of(entryOf(30, 30)));
        when(restClient.getOrderBook(eq(symbol), anyInt()))
                .thenReturn(restOrderBook);

        var manager = new BinanceOrderBookManager(restClient, webSocketClient);
        var orderBook = new TestOrderBook(symbol);

        var snapshot = orderBook.createSnapshot();
        assertTrue(snapshot.bids().isEmpty());
        assertTrue(snapshot.asks().isEmpty());

        var readyFuture = manager.manage(orderBook);
        assertFalse(readyFuture.isDone());

        var callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(webSocketClient).getDepthStream(eq(symbolLower), callbackCaptor.capture());

        var expected = entriesOf(
                linkedMapOf(3, 3),
                linkedMapOf(30, 30));
        var actual = orderBook.createSnapshot();
        assertEntries(expected, actual);

        var callback = callbackCaptor.getValue();
        callback.onEvent(eventOf(symbol, 4, entryOf(4, 4), entryOf(40, 40)));
        callback.onEvent(eventOf(symbol, 5, entryOf(5, 5), entryOf(50, 50)));

        expected = entriesOf(
                linkedMapOf(5, 5, 4, 4, 3, 3),
                linkedMapOf(30, 30, 40, 40, 50, 50));
        actual = orderBook.createSnapshot();
        assertEntries(expected, actual);

        assertTrue(readyFuture.isDone());
    }
}
