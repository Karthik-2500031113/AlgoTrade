
package com.algotrade.exchange;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderStatus;
import com.algotrade.enums.OrderType;
import com.algotrade.model.Order;
import com.algotrade.model.Trade;

public class MatchingEngine {

    private final OrderBook orderBook;

    public MatchingEngine(OrderBook orderBook) {

        if (orderBook == null) {
            throw new IllegalArgumentException(
                "Order book is required"
            );
        }

        this.orderBook = orderBook;
    }

    public synchronized List<Trade> submitOrder(Order incoming) {

        if (incoming == null) {
            throw new IllegalArgumentException(
                "Order cannot be null"
            );
        }

        if (!orderBook.getStockSymbol().equals(
                incoming.getStockSymbol())) {
            throw new IllegalArgumentException(
                "Order belongs to another stock"
            );
        }

        if (incoming.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException(
                "Only NEW orders can be submitted"
            );
        }

        incoming.activate();

        List<Trade> executedTrades = new ArrayList<>();

        boolean selfTradeDetected = false;

        while (incoming.getRemainingQuantity() > 0) {

            Optional<Order> bestOppositeOrder =
                incoming.getSide() == OrderSide.BUY
                    ? orderBook.getBestAsk()
                    : orderBook.getBestBid();

            if (bestOppositeOrder.isEmpty()) {
                break;
            }

            Order resting = bestOppositeOrder.get();

            if (!isPriceCompatible(incoming, resting)) {
                break;
            }

            // Prevent self-trading
            if (incoming.getTraderId().equals(
                    resting.getTraderId())) {

                selfTradeDetected = true;
                break;
            }

            int executionQuantity = Math.min(
                incoming.getRemainingQuantity(),
                resting.getRemainingQuantity()
            );

            // Resting order determines execution price
            BigDecimal executionPrice = resting.getLimitPrice();

            Order buyOrder =
                incoming.getSide() == OrderSide.BUY
                    ? incoming : resting;

            Order sellOrder =
                incoming.getSide() == OrderSide.SELL
                    ? incoming : resting;

            Trade trade = new Trade(
                buyOrder,
                sellOrder,
                executionPrice,
                executionQuantity
            );

            // Execute both orders
            incoming.execute(executionQuantity);
            resting.execute(executionQuantity);

            // Remove fully filled resting order
            if (resting.getStatus() == OrderStatus.FILLED) {
                orderBook.removeFilledOrder(
                    resting.getOrderId()
                );
            }

            executedTrades.add(trade);
        }

        if (selfTradeDetected) {
            incoming.cancel();
        } else if (incoming.getRemainingQuantity() > 0) {

            if (incoming.getType() == OrderType.LIMIT) {
                orderBook.addRemainder(incoming);
            } else {
                // Cancel unfilled market quantity
                incoming.cancel();
            }
        }

        return List.copyOf(executedTrades);
    }

    private boolean isPriceCompatible(
            Order incoming,
            Order resting) {

        if (incoming.getType() == OrderType.MARKET) {
            return true;
        }

        int comparison = incoming.getLimitPrice()
            .compareTo(resting.getLimitPrice());

        if (incoming.getSide() == OrderSide.BUY) {
            return comparison >= 0;
        }

        return comparison <= 0;
    }
}
