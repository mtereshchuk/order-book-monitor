package com.mtereshchuk.assignment.core.impl;

import com.mtereshchuk.assignment.core.OrderBookStorage;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.mtereshchuk.assignment.utils.TestUtils.*;

/**
 * @author mtereshchuk
 */
public class InMemoryOrderBookTest {
    @Test
    public void baseOperationsTest() {
        var limit = 3;
        OrderBookStorage orderBook = new InMemoryOrderBook("ETHUSDT", limit);

        var expected = entriesOf(Map.of(), Map.of());
        var actual = orderBook.createSnapshot(limit);
        assertEntries(expected, actual); // empty

        orderBook.batchUpdate(List.of(), List.of(), entriesOf(
                Map.of(10.0, 1.0, 20.0, 2.0, 30.0, 3.0),
                Map.of(60.0, 6.0, 50.0, 5.0, 40.0, 4.0)));

        expected = entriesOf(
                linkedMapOf(30.0, 3.0, 20.0, 2.0, 10.0, 1.0), // order is matter
                linkedMapOf(40.0, 4.0, 50.0, 5.0, 60.0, 6.0));
        actual = orderBook.createSnapshot(limit);
        assertEntries(expected, actual); // correct sort

        orderBook.batchUpdate(
                List.of(BigDecimal.valueOf(20.0)),
                List.of(BigDecimal.valueOf(50.0)),
                entriesOf(Map.of(), Map.of()));

        expected = entriesOf(
                linkedMapOf(30.0, 3.0, 10.0, 1.0),
                linkedMapOf(40.0, 4.0, 60.0, 6.0));
        actual = orderBook.createSnapshot(limit);
        assertEntries(expected, actual); // removed

        orderBook.batchUpdate(List.of(), List.of(), entriesOf(
                Map.of(20.0, 2.0, 110.0, 11.0),
                Map.of(50.0, 5.0, 35.0, 3.5)));

        expected = entriesOf(
                linkedMapOf(110.0, 11.0, 30.0, 3.0, 20.0, 2.0),
                linkedMapOf(35.0, 3.5, 40.0, 4.0, 50.0, 5.0));
        actual = orderBook.createSnapshot(limit);
        assertEntries(expected, actual); // new entries
    }
}
