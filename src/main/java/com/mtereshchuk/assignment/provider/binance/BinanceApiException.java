package com.mtereshchuk.assignment.provider.binance;

import com.mtereshchuk.assignment.provider.binance.json.BinanceRestError;

/**
 * @author mtereshchuk
 */
public class BinanceApiException extends RuntimeException {
    public BinanceApiException(Throwable cause) {
        super(cause);
    }

    public BinanceApiException(BinanceRestError error) {
        super(error.message());
    }
}
