
package com.algotrade.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderStatus;
import com.algotrade.enums.OrderType;
import com.algotrade.util.Money;

public class Trade {

    private final String tradeId;
    private final String buyOrderId;
    private final String sellOrderId;
    private final String buyerId;
    private final String sellerId;
    private final String stockSymbol;
    private final BigDecimal executionPrice;
    private final int quantity;
    private final Instant executedAt;

    // ================= NORMAL CONSTRUCTOR =================

    public Trade(
            Order buyOrder,
            Order sellOrder,
            BigDecimal executionPrice,
            int quantity) {

        if (buyOrder == null || sellOrder == null) {
            throw new IllegalArgumentException(
                "Both orders are required"
            );
        }

        if (buyOrder.getSide() != OrderSide.BUY ||
            sellOrder.getSide() != OrderSide.SELL) {

            throw new IllegalArgumentException(
                "Trade requires one BUY and one SELL order"
            );
        }

        if (!buyOrder.getStockSymbol().equals(
                sellOrder.getStockSymbol())) {

            throw new IllegalArgumentException(
                "Orders must belong to the same stock"
            );
        }

        if (buyOrder.getTraderId().equals(
                sellOrder.getTraderId())) {

            throw new IllegalArgumentException(
                "Self-trading is not permitted"
            );
        }

        if (!isExecutable(buyOrder) || !isExecutable(sellOrder)) {
            throw new IllegalArgumentException(
                "Both orders must be active"
            );
        }

        if (quantity <= 0 ||
            quantity > buyOrder.getRemainingQuantity() ||
            quantity > sellOrder.getRemainingQuantity()) {

            throw new IllegalArgumentException(
                "Invalid trade quantity"
            );
        }

        this.executionPrice = Money.requirePositive(
            executionPrice,
            "Execution price"
        );

        if (buyOrder.getType() == OrderType.LIMIT &&
            executionPrice.compareTo(buyOrder.getLimitPrice()) > 0) {

            throw new IllegalArgumentException(
                "Execution price exceeds buyer's limit"
            );
        }

        if (sellOrder.getType() == OrderType.LIMIT &&
            executionPrice.compareTo(sellOrder.getLimitPrice()) < 0) {

            throw new IllegalArgumentException(
                "Execution price is below seller's limit"
            );
        }

        this.tradeId = UUID.randomUUID().toString();

        this.buyOrderId = buyOrder.getOrderId();
        this.sellOrderId = sellOrder.getOrderId();

        this.buyerId = buyOrder.getTraderId();
        this.sellerId = sellOrder.getTraderId();

        this.stockSymbol = buyOrder.getStockSymbol();

        this.quantity = quantity;
        this.executedAt = Instant.now();
    }

    // ================= RESTORATION CONSTRUCTOR =================

    public Trade(
            String tradeId,
            String buyOrderId,
            String sellOrderId,
            String buyerId,
            String sellerId,
            String stockSymbol,
            BigDecimal executionPrice,
            int quantity,
            Instant executedAt) {

        if (tradeId == null || tradeId.isBlank() ||
            buyOrderId == null || buyOrderId.isBlank() ||
            sellOrderId == null || sellOrderId.isBlank() ||
            buyerId == null || buyerId.isBlank() ||
            sellerId == null || sellerId.isBlank() ||
            stockSymbol == null || stockSymbol.isBlank()) {

            throw new IllegalArgumentException(
                "Trade identifiers cannot be empty"
            );
        }

        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException(
                "Self-trading is not permitted"
            );
        }

        if (buyOrderId.equals(sellOrderId)) {
            throw new IllegalArgumentException(
                "Buy and sell order IDs must be different"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Trade quantity must be positive"
            );
        }

        this.executionPrice = Money.requirePositive(
            executionPrice,
            "Execution price"
        );

        if (executedAt == null) {
            throw new IllegalArgumentException(
                "Execution timestamp is required"
            );
        }

        this.tradeId = tradeId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.stockSymbol = stockSymbol.trim().toUpperCase();
        this.quantity = quantity;
        this.executedAt = executedAt;
    }

    // ================= HELPERS =================

    private boolean isExecutable(Order order) {

        return order.getStatus() == OrderStatus.OPEN ||
               order.getStatus() == OrderStatus.PARTIALLY_FILLED;
    }

    // ================= GETTERS =================

    public String getTradeId() {
        return tradeId;
    }

    public String getBuyOrderId() {
        return buyOrderId;
    }

    public String getSellOrderId() {
        return sellOrderId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public BigDecimal getExecutionPrice() {
        return executionPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public BigDecimal getTotalValue() {

        return executionPrice.multiply(
            BigDecimal.valueOf(quantity)
        );
    }

    // ================= TO STRING =================

    @Override
    public String toString() {

        return "Trade{" +
            "tradeId='" + tradeId + '\'' +
            ", stock='" + stockSymbol + '\'' +
            ", buyer='" + buyerId + '\'' +
            ", seller='" + sellerId + '\'' +
            ", executionPrice=" + executionPrice +
            ", quantity=" + quantity +
            ", totalValue=" + getTotalValue() +
            '}';
    }
}
