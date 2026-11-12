
package com.algotrade.model;

import java.math.BigDecimal;
import java.util.Objects;

import com.algotrade.util.Money;

public class Stock {

    private final String symbol;
    private final String companyName;

    private BigDecimal marketPrice;
    private BigDecimal previousClose;

    private long totalTradedQuantity;

    public Stock(
            String symbol,
            String companyName,
            BigDecimal marketPrice) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                "Stock symbol is required"
            );
        }

        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException(
                "Company name is required"
            );
        }

        this.symbol = symbol.trim().toUpperCase();
        this.companyName = companyName.trim();

        this.marketPrice =
            Money.requirePositive(marketPrice, "Market price");

        this.previousClose = marketPrice;
        this.totalTradedQuantity = 0;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public BigDecimal getMarketPrice() {
        return marketPrice;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public long getTotalTradedQuantity() {
        return totalTradedQuantity;
    }

    public void updateMarketPrice(BigDecimal newPrice) {

        this.marketPrice =
            Money.requirePositive(newPrice, "New market price");
    }

    public void recordTrade(
            BigDecimal executionPrice,
            long quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Trade quantity must be positive"
            );
        }

        updateMarketPrice(executionPrice);

        totalTradedQuantity =
            Math.addExact(totalTradedQuantity, quantity);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;

        if (!(obj instanceof Stock other)) return false;

        return symbol.equals(other.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }
}
