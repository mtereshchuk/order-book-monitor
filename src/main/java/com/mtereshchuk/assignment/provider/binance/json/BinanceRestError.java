package com.mtereshchuk.assignment.provider.binance.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mtereshchuk
 */
public record BinanceRestError(int code, @JsonProperty("msg") String message) {
}
