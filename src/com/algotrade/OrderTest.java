
package com.algotrade;

import java.math.BigDecimal;

import com.algotrade.model.Order;
import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderType;

public class OrderTest {

    public static void main(String[] args) {

        Order order = new Order(
            "TRADER001",
            "TCS",
            OrderSide.BUY,
            OrderType.LIMIT,
            new BigDecimal("3450.00"),
            100
        );

        System.out.println("Initial Order:");
        System.out.println(order);

        order.activate();

        System.out.println("\nAfter Activation:");
        System.out.println(order.getStatus());

        order.execute(40);

        System.out.println("\nAfter Partial Execution:");
        System.out.println("Executed: "
            + order.getExecutedQuantity());

        System.out.println("Remaining: "
            + order.getRemainingQuantity());

        System.out.println("Status: "
            + order.getStatus());

        order.execute(60);

        System.out.println("\nAfter Final Execution:");
        System.out.println("Status: "
            + order.getStatus());
    }
}
