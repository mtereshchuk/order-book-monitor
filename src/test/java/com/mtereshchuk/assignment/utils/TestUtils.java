package com.mtereshchuk.assignment.utils;

import com.mtereshchuk.assignment.core.OrderBookStorage;
import com.mtereshchuk.assignment.provider.binance.json.DepthEvent;
import com.mtereshchuk.assignment.provider.binance.json.OrderBook;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mtereshchuk
 */
public final class TestUtils {
    private TestUtils() {
    }

    public static OrderBook.Entry entryOf(double price, double size) {
        return new OrderBook.Entry(BigDecimal.valueOf(price), BigDecimal.valueOf(size));
    }

    public static DepthEvent eventOf(String symbol, int finalUpdateId, OrderBook.Entry bid, OrderBook.Entry ask) {
        return new DepthEvent("depthUpdate", 42, symbol, -1, finalUpdateId, List.of(bid), List.of(ask));
    }

    public static OrderBookStorage.Entries entriesOf(Map<Double, Double> bids, Map<Double, Double> asks) {
        return new OrderBookStorage.Entries(toDecimalMap(bids), toDecimalMap(asks));
    }

    public static Map<Double, Double> linkedMapOf(double k1, double v1) {
        var linkedMap = new LinkedHashMap<Double, Double>();
        linkedMap.put(k1, v1);
        return linkedMap;
    }

    public static Map<Double, Double> linkedMapOf(double k1, double v1, double k2, double v2) {
        var linkedMap = linkedMapOf(k1, v1);
        linkedMap.put(k2, v2);
        return linkedMap;
    }

    public static Map<Double, Double> linkedMapOf(double k1, double v1, double k2, double v2, double k3, double v3) {
        var linkedMap = linkedMapOf(k1, v1, k2, v2);
        linkedMap.put(k3, v3);
        return linkedMap;
    }

    public static Map<BigDecimal, BigDecimal> toDecimalMap(Map<Double, Double> entryMap) {
        var linkedMap = new LinkedHashMap<BigDecimal, BigDecimal>();
        for (var entry : entryMap.entrySet()) {
            linkedMap.put(
                    BigDecimal.valueOf(entry.getKey()),
                    BigDecimal.valueOf(entry.getValue()));
        }
        return linkedMap;
    }

    public static void assertEntries(OrderBookStorage.Entries expected, OrderBookStorage.Entries actual) {
        assertMapEquals(expected.bids(), actual.bids());
        assertMapEquals(expected.asks(), actual.asks());
    }

    public static <K, V> void assertMapEquals(Map<K, V> expected, Map<K, V> actual) { // let's do it without Guava
        assertEquals(expected.size(), actual.size());
        if (expected.size() > 0) {
            var expectedIterator = expected.entrySet().iterator();
            var actualIterator = actual.entrySet().iterator();

            while (expectedIterator.hasNext()) {
                assertTrue(actualIterator.hasNext());

                var expectedEntry = expectedIterator.next();
                var actualEntry = actualIterator.next();

                assertEquals(expectedEntry.getKey(), actualEntry.getKey());
                assertEquals(expectedEntry.getValue(), actualEntry.getValue());
            }
        }
    }
}
