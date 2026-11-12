
package com.algotrade.exchange;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderStatus;
import com.algotrade.enums.OrderType;
import com.algotrade.model.Order;

public class OrderBook {

    private final String stockSymbol;

    // Highest buy price first
    private final TreeMap<BigDecimal, ArrayDeque<Order>> buyOrders;

    // Lowest sell price first
    private final TreeMap<BigDecimal, ArrayDeque<Order>> sellOrders;

    // Active order ID -> Order
    private final Map<String, Order> activeOrders;

    public OrderBook(String stockSymbol) {

        if (stockSymbol == null || stockSymbol.isBlank()) {
            throw new IllegalArgumentException(
                "Stock symbol is required"
            );
        }

        this.stockSymbol = stockSymbol.trim().toUpperCase();

        this.buyOrders = new TreeMap<>(Comparator.reverseOrder());
        this.sellOrders = new TreeMap<>();

        this.activeOrders = new HashMap<>();
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    // ================= ADD NEW ORDER =================

    public void addOrder(Order order) {

        validateOrder(order);

        if (order.getType() != OrderType.LIMIT) {
            throw new IllegalArgumentException(
                "Only limit orders can enter the order book"
            );
        }

        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException(
                "Only NEW orders can be submitted"
            );
        }

        if (activeOrders.containsKey(order.getOrderId())) {
            throw new IllegalArgumentException(
                "Order already exists"
            );
        }

        TreeMap<BigDecimal, ArrayDeque<Order>> book =
            getBook(order.getSide());

        BigDecimal price = order.getLimitPrice();

        ArrayDeque<Order> queue =
            book.computeIfAbsent(
                price,
                key -> new ArrayDeque<>()
            );

        order.activate();

        queue.addLast(order);

        activeOrders.put(order.getOrderId(), order);
    }

    // ================= RESTORE SAVED ORDER =================

    public void restoreOrder(Order order) {

        validateOrder(order);

        if (order.getType() != OrderType.LIMIT) {
            throw new IllegalArgumentException(
                "Only limit orders can be restored"
            );
        }

        if (order.getStatus() != OrderStatus.OPEN
                && order.getStatus() != OrderStatus.PARTIALLY_FILLED) {

            throw new IllegalStateException(
                "Only OPEN or PARTIALLY_FILLED orders can be restored"
            );
        }

        if (order.getRemainingQuantity() <= 0) {
            throw new IllegalArgumentException(
                "Restored order must have remaining quantity"
            );
        }

        if (activeOrders.containsKey(order.getOrderId())) {
            throw new IllegalArgumentException(
                "Order already exists: " + order.getOrderId()
            );
        }

        TreeMap<BigDecimal, ArrayDeque<Order>> book =
            getBook(order.getSide());

        BigDecimal price = order.getLimitPrice();

        ArrayDeque<Order> queue =
            book.computeIfAbsent(
                price,
                key -> new ArrayDeque<>()
            );

        // Preserve FIFO priority using original sequence number.
        ArrayDeque<Order> reorderedQueue = new ArrayDeque<>();

        boolean inserted = false;

        for (Order existing : queue) {

            if (!inserted
                    && order.getSequenceNumber()
                        < existing.getSequenceNumber()) {

                reorderedQueue.addLast(order);
                inserted = true;
            }

            reorderedQueue.addLast(existing);
        }

        if (!inserted) {
            reorderedQueue.addLast(order);
        }

        book.put(price, reorderedQueue);

        // Do not activate again. Preserve restored status.
        activeOrders.put(order.getOrderId(), order);
    }

    // ================= BEST BID / ASK =================

    public Optional<Order> getBestBid() {
        return getBestOrder(buyOrders);
    }

    public Optional<Order> getBestAsk() {
        return getBestOrder(sellOrders);
    }

    private Optional<Order> getBestOrder(
            TreeMap<BigDecimal, ArrayDeque<Order>> book) {

        Map.Entry<BigDecimal, ArrayDeque<Order>> entry =
            book.firstEntry();

        if (entry == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(entry.getValue().peekFirst());
    }

    // ================= FIND ORDER =================

    public Optional<Order> findOrder(String orderId) {

        if (orderId == null || orderId.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(activeOrders.get(orderId));
    }

    // ================= CANCEL ORDER =================

    public boolean cancelOrder(String orderId) {

        Order order = activeOrders.get(orderId);

        if (order == null) {
            return false;
        }

        TreeMap<BigDecimal, ArrayDeque<Order>> book =
            getBook(order.getSide());

        BigDecimal price = order.getLimitPrice();

        ArrayDeque<Order> queue = book.get(price);

        if (queue == null || !queue.remove(order)) {
            throw new IllegalStateException(
                "Order book index is inconsistent"
            );
        }

        if (queue.isEmpty()) {
            book.remove(price);
        }

        order.cancel();

        activeOrders.remove(orderId);

        return true;
    }

    // ================= REMOVE FILLED ORDER =================

    public void removeFilledOrder(String orderId) {

        Order order = activeOrders.get(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                "Active order not found: " + orderId
            );
        }

        if (order.getStatus() != OrderStatus.FILLED) {
            throw new IllegalStateException(
                "Order is not fully filled"
            );
        }

        TreeMap<BigDecimal, ArrayDeque<Order>> book =
            getBook(order.getSide());

        BigDecimal price = order.getLimitPrice();

        ArrayDeque<Order> queue = book.get(price);

        if (queue == null || !queue.remove(order)) {
            throw new IllegalStateException(
                "Order book index is inconsistent"
            );
        }

        if (queue.isEmpty()) {
            book.remove(price);
        }

        activeOrders.remove(orderId);
    }

    // ================= ORDER COUNTS =================

    public int getActiveOrderCount() {
        return activeOrders.size();
    }

    public int getBuyOrderCount() {
        return countOrders(buyOrders);
    }

    public int getSellOrderCount() {
        return countOrders(sellOrders);
    }

    private int countOrders(
            TreeMap<BigDecimal, ArrayDeque<Order>> book) {

        int count = 0;

        for (ArrayDeque<Order> queue : book.values()) {
            count = Math.addExact(count, queue.size());
        }

        return count;
    }

    // ================= GET ORDERS =================

    public List<Order> getOrders(OrderSide side) {

        if (side == null) {
            throw new IllegalArgumentException(
                "Order side is required"
            );
        }

        TreeMap<BigDecimal, ArrayDeque<Order>> book =
            getBook(side);

        List<Order> result = new ArrayList<>();

        for (ArrayDeque<Order> queue : book.values()) {
            result.addAll(queue);
        }

        return Collections.unmodifiableList(result);
    }

    // ================= INTERNAL HELPERS =================

    private TreeMap<BigDecimal, ArrayDeque<Order>> getBook(
            OrderSide side) {

        if (side == OrderSide.BUY) {
            return buyOrders;
        }

        if (side == OrderSide.SELL) {
            return sellOrders;
        }

        throw new IllegalArgumentException(
            "Unsupported order side"
        );
    }

    private void validateOrder(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                "Order cannot be null"
            );
        }

        if (!stockSymbol.equals(order.getStockSymbol())) {
            throw new IllegalArgumentException(
                "Order belongs to a different stock"
            );
        }

        if (order.getLimitPrice() == null) {
            throw new IllegalArgumentException(
                "Limit price is required"
            );
        }
    }

    // ================= ADD EXISTING REMAINDER =================

    public void addRemainder(Order order) {

        validateOrder(order);

        if (order.getType() != OrderType.LIMIT) {
            throw new IllegalArgumentException(
                "Only limit orders can rest in the book"
            );
        }

        if (order.getStatus() != OrderStatus.OPEN
                && order.getStatus() != OrderStatus.PARTIALLY_FILLED) {

            throw new IllegalStateException(
                "Order must be active"
            );
        }

        if (activeOrders.containsKey(order.getOrderId())) {
            throw new IllegalArgumentException(
                "Order already exists"
            );
        }

        TreeMap<BigDecimal, ArrayDeque<Order>> book =
            getBook(order.getSide());

        ArrayDeque<Order> queue =
            book.computeIfAbsent(
                order.getLimitPrice(),
                key -> new ArrayDeque<>()
            );

        queue.addLast(order);

        activeOrders.put(order.getOrderId(), order);
    }
}
