package com.mtereshchuk.assignment.provider.binance.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtereshchuk.assignment.provider.binance.BinanceApiConfig;
import com.mtereshchuk.assignment.provider.binance.BinanceApiException;
import com.mtereshchuk.assignment.provider.binance.BinanceRestClient;
import com.mtereshchuk.assignment.provider.binance.json.BinanceRestError;
import com.mtereshchuk.assignment.provider.binance.json.OrderBook;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * @author mtereshchuk
 */
public class BinanceRestClientImpl implements BinanceRestClient {
    private static final String DEPTH_URL_FORMATTER = BinanceApiConfig.REST_URL + "api/v3/depth?symbol=%s&limit=%d";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BinanceRestClientImpl(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public OrderBook getOrderBook(String symbol, int limit) {
        try {
            var request = buildRequest(symbol, limit);
            var response = httpClient.send(request, BodyHandlers.ofString());
            var responseBody = response.body();

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                return parseBody(responseBody, OrderBook.class);
            }

            var restError = parseBody(responseBody, BinanceRestError.class);
            throw new BinanceApiException(restError);
        } catch (IOException | InterruptedException e) {
            throw new BinanceApiException(e);
        }
    }

    private HttpRequest buildRequest(String symbol, int limit) {
        return HttpRequest.newBuilder()
                .uri(URI.create(String.format(DEPTH_URL_FORMATTER, symbol, limit)))
                .GET()
                .build();
    }

    private <T> T parseBody(String responseBody, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(responseBody, clazz);
    }
}
