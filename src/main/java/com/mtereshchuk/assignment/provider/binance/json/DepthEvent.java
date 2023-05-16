package com.mtereshchuk.assignment.provider.binance.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author mtereshchuk
 */
public record DepthEvent(
        @JsonProperty("e") String eventType,
        @JsonProperty("E") long eventTime,
        @JsonProperty("s") String symbol,
        @JsonProperty("U") long firstUpdateId,
        @JsonProperty("u") long finalUpdateId,
        @JsonProperty("b") List<OrderBook.Entry> bids,
        @JsonProperty("a") List<OrderBook.Entry> asks
) {
}
