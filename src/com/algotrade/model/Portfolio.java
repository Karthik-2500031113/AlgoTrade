
package com.algotrade.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.algotrade.util.Money;

public class Portfolio {

    private BigDecimal cashBalance;

    private final Map<String, Holding> holdings;

    public Portfolio(BigDecimal initialBalance) {

        this.cashBalance = Money.requireNonNegative(
            initialBalance, "Initial balance"
        );

        this.holdings = new HashMap<>();
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public Map<String, Holding> getHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public Holding getHolding(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }

        return holdings.get(symbol.trim().toUpperCase());
    }

    public void deposit(BigDecimal amount) {

        Money.requirePositive(amount, "Deposit");

        cashBalance = cashBalance.add(amount);
    }

    public void withdraw(BigDecimal amount) {

        Money.requirePositive(amount, "Withdrawal");

        if (cashBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                "Insufficient cash balance"
            );
        }

        cashBalance = cashBalance.subtract(amount);
    }

    public void addHolding(
            String symbol,
            int quantity,
            BigDecimal price) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                "Stock symbol required"
            );
        }

        String key = symbol.trim().toUpperCase();

        Holding holding = holdings.get(key);

        if (holding == null) {
            holdings.put(
                key,
                new Holding(key, quantity, price)
            );
        } else {
            holding.addShares(quantity, price);
        }
    }

    public void sellHolding(String symbol, int quantity) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                "Stock symbol required"
            );
        }

        String key = symbol.trim().toUpperCase();

        Holding holding = holdings.get(key);

        if (holding == null) {
            throw new IllegalArgumentException(
                "Stock not found"
            );
        }

        holding.removeShares(quantity);

        if (holding.getQuantity() == 0) {
            holdings.remove(key);
        }
    }

    public BigDecimal calculateMarketValue(
            Map<String, BigDecimal> marketPrices) {

        BigDecimal total = BigDecimal.ZERO;

        for (Holding holding : holdings.values()) {

            BigDecimal price = marketPrices.get(
                holding.getStockSymbol()
            );

            if (price == null) {
                throw new IllegalArgumentException(
                    "Missing market price for "
                    + holding.getStockSymbol()
                );
            }

            Money.requireNonNegative(price, "Market price");

            BigDecimal value = price.multiply(
                BigDecimal.valueOf(holding.getQuantity())
            );

            total = total.add(value);
        }

        return total;
    }

    public BigDecimal calculateTotalValue(
            Map<String, BigDecimal> marketPrices) {

        return cashBalance.add(
            calculateMarketValue(marketPrices)
        );
    }
}
