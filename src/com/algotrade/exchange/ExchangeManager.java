
package com.algotrade.exchange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.algotrade.enums.OrderSide;
import com.algotrade.model.Order;
import com.algotrade.model.Stock;
import com.algotrade.model.Trade;
import com.algotrade.model.Trader;

public class ExchangeManager {

    private final StockExchange stockExchange;
    private final SettlementEngine settlementEngine;

    private final Map<String, OrderBook> orderBooks;
    private final Map<String, MatchingEngine> matchingEngines;
    private final Map<String, Trader> traders;

    private final List<Trade> tradeHistory;

    public ExchangeManager() {

        stockExchange = new StockExchange();
        settlementEngine = new SettlementEngine();

        orderBooks = new HashMap<>();
        matchingEngines = new HashMap<>();
        traders = new HashMap<>();

        tradeHistory = new ArrayList<>();
    }

    // ================= STOCK MANAGEMENT =================

    public synchronized void registerStock(Stock stock) {

        if (stock == null) {
            throw new IllegalArgumentException(
                "Stock is required"
            );
        }

        String symbol = stock.getSymbol();

        if (orderBooks.containsKey(symbol)) {
            throw new IllegalArgumentException(
                "Stock already registered: " + symbol
            );
        }

        stockExchange.registerStock(stock);

        OrderBook book = new OrderBook(symbol);

        orderBooks.put(symbol, book);
        matchingEngines.put(
            symbol,
            new MatchingEngine(book)
        );
    }

    // ================= TRADER MANAGEMENT =================

    public synchronized void registerTrader(Trader trader) {

        if (trader == null) {
            throw new IllegalArgumentException(
                "Trader is required"
            );
        }

        String traderId = trader.getTraderId();

        if (traders.containsKey(traderId)) {
            throw new IllegalArgumentException(
                "Trader already registered: " + traderId
            );
        }

        traders.put(traderId, trader);
    }

    public synchronized Trader getTrader(String traderId) {

        Trader trader = traders.get(traderId);

        if (trader == null) {
            throw new IllegalArgumentException(
                "Unknown trader: " + traderId
            );
        }

        return trader;
    }

    // ================= ORDER MANAGEMENT =================

    public synchronized List<Trade> submitOrder(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                "Order is required"
            );
        }

        String symbol = order.getStockSymbol();

        MatchingEngine engine = matchingEngines.get(symbol);

        if (engine == null) {
            throw new IllegalArgumentException(
                "Stock is not registered: " + symbol
            );
        }

        Trader incomingTrader = traders.get(order.getTraderId());

        if (incomingTrader == null) {
            throw new IllegalArgumentException(
                "Trader is not registered"
            );
        }

        List<Trade> executedTrades = engine.submitOrder(order);

        for (Trade trade : executedTrades) {

            Trader buyer = traders.get(trade.getBuyerId());
            Trader seller = traders.get(trade.getSellerId());

            if (buyer == null || seller == null) {
                throw new IllegalStateException(
                    "Trade participant is not registered"
                );
            }

            settlementEngine.settleTrade(
                trade,
                buyer,
                seller
            );

            tradeHistory.add(trade);
        }

        return List.copyOf(executedTrades);
    }

    // ================= ORDER RESTORATION =================

    public synchronized void restoreOrder(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                "Order is required"
            );
        }

        OrderBook book = orderBooks.get(
            order.getStockSymbol()
        );

        if (book == null) {
            throw new IllegalArgumentException(
                "Stock is not registered: "
                + order.getStockSymbol()
            );
        }

        if (!traders.containsKey(order.getTraderId())) {
            throw new IllegalArgumentException(
                "Order trader is not registered: "
                + order.getTraderId()
            );
        }

        book.restoreOrder(order);
    }

    // ================= TRADE HISTORY =================

    public synchronized List<Trade> getTradeHistory() {

        return List.copyOf(tradeHistory);
    }

    // ================= TRADE RESTORATION =================

    public synchronized void restoreTradeHistory(
            List<Trade> trades) {

        if (trades == null) {
            throw new IllegalArgumentException(
                "Trade list is required"
            );
        }

        for (Trade trade : trades) {

            if (trade == null) {
                throw new IllegalArgumentException(
                    "Trade cannot be null"
                );
            }

            if (!traders.containsKey(trade.getBuyerId())
                    || !traders.containsKey(trade.getSellerId())) {

                throw new IllegalArgumentException(
                    "Trade participant is not registered"
                );
            }

            if (!stockExchange.containsStock(
                    trade.getStockSymbol())) {

                throw new IllegalArgumentException(
                    "Trade stock is not registered: "
                    + trade.getStockSymbol()
                );
            }
        }

        tradeHistory.addAll(trades);
    }

    // ================= EXCHANGE INFORMATION =================

    public synchronized int getTraderCount() {

        return traders.size();
    }

    public synchronized int getRegisteredStockCount() {

        return orderBooks.size();
    }

    public synchronized OrderBook getOrderBook(String symbol) {

        if (symbol == null) {
            throw new IllegalArgumentException(
                "Stock symbol is required"
            );
        }

        OrderBook book = orderBooks.get(
            symbol.toUpperCase()
        );

        if (book == null) {
            throw new IllegalArgumentException(
                "Unknown stock: " + symbol
            );
        }

        return book;
    }

    // ================= PERSISTENCE GETTERS =================

    public synchronized Collection<Stock> getAllStocks() {

        return List.copyOf(
            stockExchange.getAllStocks()
        );
    }

    public synchronized List<Trader> getAllTraders() {

        return List.copyOf(traders.values());
    }

    public synchronized List<Order> getAllOrders() {

        List<Order> orders = new ArrayList<>();

        for (OrderBook book : orderBooks.values()) {

            orders.addAll(
                book.getOrders(OrderSide.BUY)
            );

            orders.addAll(
                book.getOrders(OrderSide.SELL)
            );
        }

        return List.copyOf(orders);
    }
}
