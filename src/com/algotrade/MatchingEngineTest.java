
package com.algotrade;

import java.math.BigDecimal;
import java.util.List;

import com.algotrade.exchange.OrderBook;
import com.algotrade.exchange.MatchingEngine;
import com.algotrade.model.Order;
import com.algotrade.model.Trade;
import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderType;

public class MatchingEngineTest {

    public static void main(String[] args) {

        OrderBook book = new OrderBook("TCS");

        MatchingEngine engine = new MatchingEngine(book);

        // Seller 1: 40 shares at 3450
        Order sell1 = new Order(
            "SELLER001", "TCS",
            OrderSide.SELL, OrderType.LIMIT,
            new BigDecimal("3450"), 40
        );

        // Seller 2: 60 shares at 3460
        Order sell2 = new Order(
            "SELLER002", "TCS",
            OrderSide.SELL, OrderType.LIMIT,
            new BigDecimal("3460"), 60
        );

        book.addOrder(sell1);
        book.addOrder(sell2);

        // Buyer wants 100 shares at maximum 3500
        Order buy = new Order(
            "BUYER001", "TCS",
            OrderSide.BUY, OrderType.LIMIT,
            new BigDecimal("3500"), 100
        );

        List<Trade> trades = engine.submitOrder(buy);

        System.out.println("===== ALGOTRADE MATCHING ENGINE =====");

        for (Trade trade : trades) {
            System.out.println("\nTRADE EXECUTED");
            System.out.println("Price: " +
                trade.getExecutionPrice());
            System.out.println("Quantity: " +
                trade.getQuantity());
            System.out.println("Value: " +
                trade.getTotalValue());
        }

        System.out.println("\nTotal Trades: " + trades.size());

        System.out.println("Executed Quantity: " +
            buy.getExecutedQuantity());

        System.out.println("Remaining Quantity: " +
            buy.getRemainingQuantity());

        System.out.println("Buyer Status: " +
            buy.getStatus());

        System.out.println("Active Orders: " +
            book.getActiveOrderCount());
    }
}
