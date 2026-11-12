
package com.algotrade.persistence;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderStatus;
import com.algotrade.enums.OrderType;
import com.algotrade.model.Order;

public class OrderRepository {

    private final Path filePath;

    private static final String[] HEADER = {
        "orderId", "sequenceNumber", "traderId", "symbol",
        "side", "type", "limitPrice", "originalQuantity",
        "remainingQuantity", "createdAt", "status"
    };

    public OrderRepository(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException(
                "File path is required");
        }
        this.filePath = filePath;
    }

    public void saveAll(List<Order> orders) throws IOException {

        if (orders == null) {
            throw new IllegalArgumentException(
                "Order list is required");
        }

        List<String[]> rows = new ArrayList<>();
        rows.add(HEADER.clone());

        for (Order order : orders) {
            if (order == null) {
                throw new IllegalArgumentException(
                    "Order cannot be null");
            }

            rows.add(new String[] {
                order.getOrderId(),
                String.valueOf(order.getSequenceNumber()),
                order.getTraderId(),
                order.getStockSymbol(),
                order.getSide().name(),
                order.getType().name(),
                order.getLimitPrice() == null
                    ? "" : order.getLimitPrice().toPlainString(),
                String.valueOf(order.getOriginalQuantity()),
                String.valueOf(order.getRemainingQuantity()),
                order.getCreatedAt().toString(),
                order.getStatus().name()
            });
        }

        CsvUtil.write(filePath, rows);
    }

    public List<String[]> loadAll() throws IOException {

        List<String[]> rows = CsvUtil.read(filePath);

        if (rows.isEmpty()) {
            return rows;
        }

        if (!Arrays.equals(rows.get(0), HEADER)) {
            throw new IOException("Invalid orders CSV header");
        }

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);

            if (row.length != 11) {
                throw new IOException(
                    "Invalid order record at row " + (i + 1));
            }

            try {
                loadOrder(row);
            } catch (RuntimeException e) {
                throw new IOException(
                    "Invalid order at CSV row " + (i + 1), e);
            }
        }

        return rows;
    }

    // Reconstruct actual Order objects
    public List<Order> loadOrders() throws IOException {

        List<String[]> rows = loadAll();
        List<Order> orders = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            try {
                orders.add(loadOrder(rows.get(i)));
            } catch (RuntimeException e) {
                throw new IOException(
                    "Could not restore order at row " + (i + 1), e);
            }
        }

        return orders;
    }

    private Order loadOrder(String[] row) {

        String orderId = row[0];
        long sequence = Long.parseLong(row[1]);
        String traderId = row[2];
        String symbol = row[3];

        OrderSide side = OrderSide.valueOf(row[4]);
        OrderType type = OrderType.valueOf(row[5]);

        BigDecimal price = row[6].isBlank()
            ? null : new BigDecimal(row[6]);

        int originalQuantity = Integer.parseInt(row[7]);
        int remainingQuantity = Integer.parseInt(row[8]);

        Instant createdAt = Instant.parse(row[9]);
        OrderStatus status = OrderStatus.valueOf(row[10]);

        return new Order(
            orderId,
            sequence,
            traderId,
            symbol,
            side,
            type,
            price,
            originalQuantity,
            remainingQuantity,
            createdAt,
            status
        );
    }

    public int getOrderCount() throws IOException {
        return loadOrders().size();
    }
}
