
package com.algotrade.exchange;

import java.math.BigDecimal;

import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderType;
import com.algotrade.model.Holding;
import com.algotrade.model.Order;
import com.algotrade.model.Stock;
import com.algotrade.model.Trader;

public class RiskManager {

    private final int maxOrderQuantity;
    private final BigDecimal maxOrderValue;

    public RiskManager(
            int maxOrderQuantity,
            BigDecimal maxOrderValue) {

        if (maxOrderQuantity <= 0 || maxOrderValue == null
                || maxOrderValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Invalid risk limits");
        }

        this.maxOrderQuantity = maxOrderQuantity;
        this.maxOrderValue = maxOrderValue;
    }

    public void validateOrder(
            Order order, Trader trader, Stock stock) {

        if (order == null || trader == null || stock == null) {
            throw new IllegalArgumentException(
                "Order, trader and stock are required");
        }

        if (!order.getTraderId().equals(trader.getTraderId())) {
            throw new IllegalArgumentException(
                "Trader does not own this order");
        }

        if (!order.getStockSymbol().equals(stock.getSymbol())) {
            throw new IllegalArgumentException(
                "Stock symbol mismatch");
        }

        if (order.getRemainingQuantity() > maxOrderQuantity) {
            throw new IllegalStateException(
                "Order quantity exceeds risk limit");
        }

        BigDecimal referencePrice =
            order.getType() == OrderType.LIMIT
                ? order.getLimitPrice()
                : stock.getMarketPrice();

        BigDecimal orderValue = referencePrice.multiply(
            BigDecimal.valueOf(order.getRemainingQuantity()));

        if (orderValue.compareTo(maxOrderValue) > 0) {
            throw new IllegalStateException(
                "Order value exceeds risk limit");
        }

        if (order.getSide() == OrderSide.BUY) {
            validateBuyingPower(trader, orderValue);
        } else {
            validateSellableQuantity(trader, order);
        }
    }

    private void validateBuyingPower(
            Trader trader, BigDecimal requiredAmount) {

        BigDecimal availableCash =
            trader.getPortfolio().getCashBalance();

        if (availableCash.compareTo(requiredAmount) < 0) {
            throw new IllegalStateException(
                "Insufficient buying power");
        }
    }

    private void validateSellableQuantity(
            Trader trader, Order order) {

        Holding holding = trader.getPortfolio()
            .getHolding(order.getStockSymbol());

        if (holding == null
                || holding.getQuantity() < order.getRemainingQuantity()) {
            throw new IllegalStateException(
                "Insufficient shares to sell");
        }
    }
}
