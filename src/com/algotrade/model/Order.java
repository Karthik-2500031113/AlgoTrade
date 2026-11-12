
package com.algotrade.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderType;
import com.algotrade.enums.OrderStatus;
import com.algotrade.util.Money;

public class Order {

    private static final AtomicLong SEQUENCE_GENERATOR =
            new AtomicLong(0);

    private final String orderId;
    private final long sequenceNumber;
    private final String traderId;
    private final String stockSymbol;

    private final OrderSide side;
    private final OrderType type;

    private final BigDecimal limitPrice;

    private final int originalQuantity;
    private int remainingQuantity;

    private final Instant createdAt;

    private OrderStatus status;

    public Order(
            String traderId,
            String stockSymbol,
            OrderSide side,
            OrderType type,
            BigDecimal limitPrice,
            int quantity) {

        if (traderId == null || traderId.isBlank()) {
            throw new IllegalArgumentException(
                "Trader ID is required"
            );
        }

        if (stockSymbol == null || stockSymbol.isBlank()) {
            throw new IllegalArgumentException(
                "Stock symbol is required"
            );
        }

        if (side == null || type == null) {
            throw new IllegalArgumentException(
                "Order side and type are required"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be positive"
            );
        }

        if (type == OrderType.LIMIT && limitPrice == null) {
            throw new IllegalArgumentException(
                "Limit order requires a price"
            );
        }

        if (type == OrderType.MARKET && limitPrice != null) {
            throw new IllegalArgumentException(
                "Market order cannot have a limit price"
            );
        }

        if (type == OrderType.LIMIT) {
            Money.requirePositive(limitPrice, "Limit price");
        }

        this.orderId = UUID.randomUUID().toString();

        this.sequenceNumber =
            SEQUENCE_GENERATOR.incrementAndGet();

        this.traderId = traderId;
        this.stockSymbol = stockSymbol.trim().toUpperCase();

        this.side = side;
        this.type = type;
        this.limitPrice = limitPrice;

        this.originalQuantity = quantity;
        this.remainingQuantity = quantity;

        this.createdAt = Instant.now();
        this.status = OrderStatus.NEW;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getTraderId() {
        return traderId;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public int getOriginalQuantity() {
        return originalQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public int getExecutedQuantity() {
        return originalQuantity - remainingQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void activate() {

        if (status != OrderStatus.NEW) {
            throw new IllegalStateException(
                "Only NEW orders can be activated"
            );
        }

        status = OrderStatus.OPEN;
    }

    public void execute(int quantity) {

        if (status != OrderStatus.OPEN &&
            status != OrderStatus.PARTIALLY_FILLED) {

            throw new IllegalStateException(
                "Order cannot be executed"
            );
        }

        if (quantity <= 0 || quantity > remainingQuantity) {
            throw new IllegalArgumentException(
                "Invalid execution quantity"
            );
        }

        remainingQuantity -= quantity;

        if (remainingQuantity == 0) {
            status = OrderStatus.FILLED;
        } else {
            status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    public void cancel() {

        if (status != OrderStatus.NEW &&
            status != OrderStatus.OPEN &&
            status != OrderStatus.PARTIALLY_FILLED) {

            throw new IllegalStateException(
                "Order cannot be cancelled"
            );
        }

        status = OrderStatus.CANCELLED;
    }

    public void reject() {

        if (status != OrderStatus.NEW) {
            throw new IllegalStateException(
                "Only NEW orders can be rejected"
            );
        }

        status = OrderStatus.REJECTED;
    }

    @Override
    public String toString() {

        return "Order{" +
            "orderId='" + orderId + '\'' +
            ", stock='" + stockSymbol + '\'' +
            ", side=" + side +
            ", type=" + type +
            ", quantity=" + originalQuantity +
            ", remaining=" + remainingQuantity +
            ", status=" + status +
            '}';
    }

    // Restore an existing order from persistent storage
    public Order(
            String orderId,
            long sequenceNumber,
            String traderId,
            String stockSymbol,
            OrderSide side,
            OrderType type,
            BigDecimal limitPrice,
            int originalQuantity,
            int remainingQuantity,
            Instant createdAt,
            OrderStatus status) {

        if (orderId == null || orderId.isBlank()
                || traderId == null || traderId.isBlank()
                || stockSymbol == null || stockSymbol.isBlank()) {
            throw new IllegalArgumentException(
                "Order identifiers are required");
        }

        if (sequenceNumber <= 0 || originalQuantity <= 0
                || remainingQuantity < 0
                || remainingQuantity > originalQuantity) {
            throw new IllegalArgumentException(
                "Invalid order sequence or quantity");
        }

        if (side == null || type == null
                || createdAt == null || status == null) {
            throw new IllegalArgumentException(
                "Order fields cannot be null");
        }

        if (type == OrderType.LIMIT) {
            Money.requirePositive(limitPrice, "Limit price");
        } else if (limitPrice != null) {
            throw new IllegalArgumentException(
                "Market order cannot have a limit price");
        }

        if (status == OrderStatus.FILLED && remainingQuantity != 0) {
            throw new IllegalArgumentException(
                "Filled order must have zero remaining quantity");
        }

        if ((status == OrderStatus.NEW || status == OrderStatus.OPEN)
                && remainingQuantity != originalQuantity) {
            throw new IllegalArgumentException(
                "Unfilled order has inconsistent quantity");
        }

        if (status == OrderStatus.PARTIALLY_FILLED
                && (remainingQuantity == 0
                || remainingQuantity == originalQuantity)) {
            throw new IllegalArgumentException(
                "Invalid partially filled order quantity");
        }

        this.orderId = orderId;
        this.sequenceNumber = sequenceNumber;
        this.traderId = traderId;
        this.stockSymbol = stockSymbol.trim().toUpperCase();
        this.side = side;
        this.type = type;
        this.limitPrice = limitPrice;
        this.originalQuantity = originalQuantity;
        this.remainingQuantity = remainingQuantity;
        this.createdAt = createdAt;
        this.status = status;

        SEQUENCE_GENERATOR.accumulateAndGet(
            sequenceNumber, Math::max);
    }

}
