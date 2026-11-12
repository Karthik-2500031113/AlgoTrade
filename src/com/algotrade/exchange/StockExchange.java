
package com.algotrade.exchange;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.algotrade.model.Stock;

public class StockExchange {

    private static final String EXCHANGE_ID = "ALGOTRADE-EX01";

    // Stock symbol -> Stock object
    private final Map<String, Stock> stocks;

    public StockExchange() {
        this.stocks = new HashMap<>();
    }

    public String getExchangeId() {
        return EXCHANGE_ID;
    }

    // Register a new stock
    public void registerStock(Stock stock) {

        if (stock == null) {
            throw new IllegalArgumentException(
                "Stock cannot be null"
            );
        }

        String symbol = normalizeSymbol(stock.getSymbol());

        if (stocks.containsKey(symbol)) {
            throw new IllegalArgumentException(
                "Stock already listed: " + symbol
            );
        }

        stocks.put(symbol, stock);
    }

    // Search for a stock
    public Optional<Stock> findStock(String symbol) {

        String key = normalizeSymbol(symbol);

        return Optional.ofNullable(stocks.get(key));
    }

    // Retrieve a stock or throw an exception
    public Stock requireStock(String symbol) {

        return findStock(symbol)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Stock not found: " + symbol
                )
            );
    }

    // Check whether a stock is listed
    public boolean containsStock(String symbol) {

        String key = normalizeSymbol(symbol);

        return stocks.containsKey(key);
    }

    // Return all listed stocks
    public Collection<Stock> getAllStocks() {

        return Collections.unmodifiableCollection(
            stocks.values()
        );
    }

    // Total number of listed stocks
    public int getStockCount() {
        return stocks.size();
    }

    // Normalize stock symbols
    private String normalizeSymbol(String symbol) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                "Stock symbol cannot be empty"
            );
        }

        return symbol.trim().toUpperCase();
    }
}
