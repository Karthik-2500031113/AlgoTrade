
package com.algotrade;

import java.math.BigDecimal;

import com.algotrade.exchange.OrderBook;
import com.algotrade.model.Order;
import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderType;

public class OrderBookTest {

    public static void main(String[] args) {

        OrderBook book = new OrderBook("TCS");

        // BUY orders
        Order buy1 = new Order(
            "TRADER001", "TCS",
            OrderSide.BUY, OrderType.LIMIT,
            new BigDecimal("3450"), 100
        );

        Order buy2 = new Order(
            "TRADER002", "TCS",
            OrderSide.BUY, OrderType.LIMIT,
            new BigDecimal("3460"), 200
        );

        Order buy3 = new Order(
            "TRADER003", "TCS",
            OrderSide.BUY, OrderType.LIMIT,
            new BigDecimal("3460"), 150
        );

        // SELL orders
        Order sell1 = new Order(
            "TRADER004", "TCS",
            OrderSide.SELL, OrderType.LIMIT,
            new BigDecimal("3470"), 100
        );

        Order sell2 = new Order(
            "TRADER005", "TCS",
            OrderSide.SELL, OrderType.LIMIT,
            new BigDecimal("3480"), 200
        );

        // Add orders
        book.addOrder(buy1);
        book.addOrder(buy2);
        book.addOrder(buy3);

        book.addOrder(sell1);
        book.addOrder(sell2);

        System.out.println("===== ALGOTRADE ORDER BOOK =====");

        System.out.println("Stock: " +
            book.getStockSymbol());

        System.out.println("Active Orders: " +
            book.getActiveOrderCount());

        // Best bid
        System.out.println("\nBest Bid: " +
            book.getBestBid().get().getLimitPrice());

        // Best ask
        System.out.println("Best Ask: " +
            book.getBestAsk().get().getLimitPrice());

        // BUY FIFO priority
        System.out.println("\nBUY QUEUE:");

        for (Order order : book.getOrders(OrderSide.BUY)) {
            System.out.println(
                order.getLimitPrice() + " | " +
                order.getTraderId() + " | " +
                order.getSequenceNumber()
            );
        }

        // SELL queue
        System.out.println("\nSELL QUEUE:");

        for (Order order : book.getOrders(OrderSide.SELL)) {
            System.out.println(
                order.getLimitPrice() + " | " +
                order.getTraderId()
            );
        }

        // Cancel an order
        System.out.println("\nCancelling BUY1: " +
            book.cancelOrder(buy1.getOrderId()));

        System.out.println("Remaining Orders: " +
            book.getActiveOrderCount());

        System.out.println("Final Best Bid: " +
            book.getBestBid().get().getLimitPrice());
    }
}
