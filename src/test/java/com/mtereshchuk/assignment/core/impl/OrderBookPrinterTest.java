package com.mtereshchuk.assignment.core.impl;

import com.mtereshchuk.assignment.core.TestOrderBook;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author mtereshchuk
 */
public class OrderBookPrinterTest {
    @Test
    public void printTest() {
        var orderBook = TestOrderBook.of("ETHUSDT",
                Map.of(99.96, 13500.0, 99.98, 2000.0, 99.99, 2400.0),
                Map.of(100.0, 1200.0, 100.02, 400.0, 100.03, 100.0, 100.04, 200.0));

        var baos = new ByteArrayOutputStream();
        var utf8 = StandardCharsets.UTF_8.name();
        final String result;

        try (var out = new PrintStream(baos, true, utf8)) {
            new OrderBookPrinter(10, false, out).print(orderBook);
            result = baos.toString(utf8);
        } catch (UnsupportedEncodingException ignored) {
            fail();
            return;
        }

        assertEquals("""
                        BID_SIZE    BID_PRICE    ASK_PRICE    ASK_SIZE
                        2400            99.99    100.00           1200
                        2000            99.98    100.02            400
                        13500           99.96    100.03            100
                                                 100.04            200

                        """,
                result);
    }
}
