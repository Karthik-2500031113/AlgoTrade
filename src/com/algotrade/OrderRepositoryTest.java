
package com.algotrade;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderType;
import com.algotrade.model.Order;
import com.algotrade.persistence.OrderRepository;

public class OrderRepositoryTest {

    public static void main(String[] args) throws Exception {

        System.out.println("===== ALGOTRADE ORDER RESTORATION TEST =====");

        // 1. Create a new order
        Order order = new Order(
            "BUYER001",
            "TCS",
            OrderSide.BUY,
            OrderType.LIMIT,
            new BigDecimal("3500"),
            100
        );

        // 2. Activate and partially execute the order
        order.activate();
        order.execute(40);

        System.out.println("\nOriginal Order:");
        System.out.println("Order ID: " + order.getOrderId());
        System.out.println("Sequence: " + order.getSequenceNumber());
        System.out.println("Status: " + order.getStatus());
        System.out.println("Remaining Quantity: "
            + order.getRemainingQuantity());

        // 3. Initialize repository
        OrderRepository repository =
            new OrderRepository(
                Path.of("data", "orders.csv"));

        // 4. Save order
        repository.saveAll(List.of(order));

        System.out.println("\nOrder saved successfully.");

        // 5. Restore order
        List<Order> restoredOrders = repository.loadOrders();

        if (restoredOrders.isEmpty()) {
            throw new AssertionError("No orders were restored");
        }

        Order restored = restoredOrders.get(0);

        System.out.println("\nRestored Order:");

        System.out.println("Order ID Preserved: "
            + order.getOrderId().equals(restored.getOrderId()));

        System.out.println("Sequence Preserved: "
            + (order.getSequenceNumber()
                == restored.getSequenceNumber()));

        System.out.println("Trader ID Preserved: "
            + order.getTraderId().equals(restored.getTraderId()));

        System.out.println("Stock Preserved: "
            + order.getStockSymbol().equals(restored.getStockSymbol()));

        System.out.println("Timestamp Preserved: "
            + order.getCreatedAt().equals(restored.getCreatedAt()));

        System.out.println("Status Preserved: "
            + (order.getStatus() == restored.getStatus()));

        System.out.println("Remaining Quantity: "
            + restored.getRemainingQuantity());

        // 6. Verify all important fields
        boolean passed =
            order.getOrderId().equals(restored.getOrderId())
            && order.getSequenceNumber()
                == restored.getSequenceNumber()
            && order.getTraderId().equals(restored.getTraderId())
            && order.getStockSymbol().equals(restored.getStockSymbol())
            && order.getCreatedAt().equals(restored.getCreatedAt())
            && order.getStatus() == restored.getStatus()
            && order.getRemainingQuantity() == 60
            && order.getOriginalQuantity()
                == restored.getOriginalQuantity()
            && order.getLimitPrice().compareTo(
                restored.getLimitPrice()) == 0;

        System.out.println("\n-----------------------------------");

        System.out.println("Order Restoration: "
            + (passed ? "PASSED" : "FAILED"));

        System.out.println("-----------------------------------");

        if (!passed) {
            throw new AssertionError(
                "Order restoration test failed");
        }
    }
}
