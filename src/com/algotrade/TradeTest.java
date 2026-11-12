
package com.algotrade;

import java.math.BigDecimal;

import com.algotrade.model.Order;
import com.algotrade.model.Trade;
import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderType;

public class TradeTest {

    public static void main(String[] args) {

        // Create buyer order
        Order buyOrder = new Order(
            "TRADER001",
            "TCS",
            OrderSide.BUY,
            OrderType.LIMIT,
            new BigDecimal("3500.00"),
            100
        );

        // Create seller order
        Order sellOrder = new Order(
            "TRADER002",
            "TCS",
            OrderSide.SELL,
            OrderType.LIMIT,
            new BigDecimal("3450.00"),
            100
        );

        // Activate both orders
        buyOrder.activate();
        sellOrder.activate();

        // Create trade at seller's price
        Trade trade = new Trade(
            buyOrder,
            sellOrder,
            new BigDecimal("3450.00"),
            100
        );

        System.out.println("===== ALGOTRADE TRADE RECEIPT =====");

        System.out.println("Trade ID: " +
            trade.getTradeId());

        System.out.println("Stock: " +
            trade.getStockSymbol());

        System.out.println("Buyer: " +
            trade.getBuyerId());

        System.out.println("Seller: " +
            trade.getSellerId());

        System.out.println("Execution Price: " +
            trade.getExecutionPrice());

        System.out.println("Quantity: " +
            trade.getQuantity());

        System.out.println("Total Value: " +
            trade.getTotalValue());

        System.out.println("Executed At: " +
            trade.getExecutedAt());
    }
}
