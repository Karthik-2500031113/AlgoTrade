
package com.algotrade.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.algotrade.util.Money;

public class Holding {

    private final String stockSymbol;

    private int quantity;

    private BigDecimal averagePurchasePrice;

    public Holding(
            String stockSymbol,
            int quantity,
            BigDecimal averagePurchasePrice) {

        if (stockSymbol == null || stockSymbol.isBlank()) {
            throw new IllegalArgumentException(
                "Stock symbol is required"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Initial quantity must be positive"
            );
        }

        this.stockSymbol = stockSymbol.trim().toUpperCase();
        this.quantity = quantity;

        this.averagePurchasePrice =
            Money.requirePositive(
                averagePurchasePrice,
                "Average purchase price"
            );
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAveragePurchasePrice() {
        return averagePurchasePrice;
    }

    public void addShares(
            int additionalQuantity,
            BigDecimal purchasePrice) {

        if (additionalQuantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be positive"
            );
        }

        Money.requirePositive(purchasePrice, "Purchase price");

        int newQuantity =
            Math.addExact(quantity, additionalQuantity);

        BigDecimal oldCost =
            averagePurchasePrice.multiply(
                BigDecimal.valueOf(quantity)
            );

        BigDecimal newCost =
            purchasePrice.multiply(
                BigDecimal.valueOf(additionalQuantity)
            );

        BigDecimal totalCost = oldCost.add(newCost);

        averagePurchasePrice =
            totalCost.divide(
                BigDecimal.valueOf(newQuantity),
                8,
                RoundingMode.HALF_UP
            );

        quantity = newQuantity;
    }

    public void removeShares(int sellQuantity) {

        if (sellQuantity <= 0 || sellQuantity > quantity) {
            throw new IllegalArgumentException(
                "Invalid selling quantity"
            );
        }

        quantity -= sellQuantity;
    }
}
