
package com.algotrade;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderType;

import com.algotrade.exchange.ExchangeManager;

import com.algotrade.model.Order;
import com.algotrade.model.Portfolio;
import com.algotrade.model.Stock;
import com.algotrade.model.Trader;
import com.algotrade.model.Trade;

import com.algotrade.persistence.StockRepository;
import com.algotrade.persistence.TraderRepository;
import com.algotrade.persistence.TradeRepository;
import com.algotrade.persistence.OrderRepository;

public class AlgoTradeApp {

    private static final Scanner scanner =
        new Scanner(System.in);

    private static ExchangeManager exchange;

    private static Trader currentTrader;

    private static final Path DATA_DIRECTORY =
        Path.of("data");

    // ================= MAIN =================

    public static void main(String[] args) {

        try {

            initializeExchange();

            System.out.println("\n==================================");
            System.out.println("          ALGOTRADE 2.0");
            System.out.println("    JAVA STOCK EXCHANGE SIMULATOR");
            System.out.println("==================================");

            boolean running = true;

            while (running) {

                System.out.println("\n1. Select Trading Account");
                System.out.println("2. Market Watch");
                System.out.println("3. Place Limit Order");
                System.out.println("4. View Portfolio");
                System.out.println("5. Trade History");
                System.out.println("6. Exit");

                int choice = readInt("Choose option: ");

                switch (choice) {

                    case 1 -> selectAccount();

                    case 2 -> marketWatch();

                    case 3 -> placeOrder();

                    case 4 -> viewPortfolio();

                    case 5 -> tradeHistory();

                    case 6 -> {

                        saveApplicationData();

                        running = false;

                        System.out.println(
                            "Thank you for using AlgoTrade!"
                        );
                    }

                    default -> System.out.println(
                        "Invalid option. Try again."
                    );
                }
            }

        } catch (IOException | RuntimeException e) {

            System.out.println(
                "Application initialization failed: "
                + e.getMessage()
            );

        } finally {

            scanner.close();
        }
    }

    // ================= INITIALIZATION =================

    private static void initializeExchange()
            throws IOException {

        exchange = new ExchangeManager();

        Files.createDirectories(DATA_DIRECTORY);

        Path stocksFile =
            DATA_DIRECTORY.resolve("stocks.csv");

        Path tradersFile =
            DATA_DIRECTORY.resolve("traders.csv");

        Path tradesFile =
            DATA_DIRECTORY.resolve("trades.csv");

        Path ordersFile =
            DATA_DIRECTORY.resolve("orders.csv");

        StockRepository stockRepository =
            new StockRepository(stocksFile);

        TraderRepository traderRepository =
            new TraderRepository(DATA_DIRECTORY);

        TradeRepository tradeRepository =
            new TradeRepository(tradesFile);

        OrderRepository orderRepository =
            new OrderRepository(ordersFile);

        boolean stocksExist = Files.exists(stocksFile);
        boolean tradersExist = Files.exists(tradersFile);

        if (stocksExist != tradersExist) {

            throw new IOException(
                "Incomplete saved data. Both stocks.csv "
                + "and traders.csv must exist."
            );
        }

        // ========== FIRST RUN ==========

        if (!stocksExist) {

            System.out.println(
                "\nCreating a new demo exchange..."
            );

            createDemoExchange();

            System.out.println(
                "Demo exchange initialized."
            );

            return;
        }

        // ========== RESTORE SAVED DATA ==========

        System.out.println(
            "\nLoading saved exchange data..."
        );

        List<Stock> stocks =
            stockRepository.loadAll();

        List<Trader> traders =
            traderRepository.loadAll();

        if (stocks.isEmpty() || traders.isEmpty()) {

            throw new IOException(
                "Saved stock or trader data is empty."
            );
        }

        // Restore stocks first.

        for (Stock stock : stocks) {
            exchange.registerStock(stock);
        }

        // Restore traders next.

        for (Trader trader : traders) {
            exchange.registerTrader(trader);
        }

        // Restore historical trades.

        if (Files.exists(tradesFile)) {

            List<Trade> savedTrades =
                tradeRepository.loadTrades();

            exchange.restoreTradeHistory(savedTrades);

            System.out.println(
                "Restored historical trades: "
                + savedTrades.size()
            );
        }

        // Restore active orders.

        if (Files.exists(ordersFile)) {

            List<Order> savedOrders =
                orderRepository.loadOrders();

            for (Order order : savedOrders) {
                exchange.restoreOrder(order);
            }

            System.out.println(
                "Restored active orders: "
                + savedOrders.size()
            );
        }

        System.out.println(
            "Saved stocks loaded: " + stocks.size()
        );

        System.out.println(
            "Saved traders loaded: " + traders.size()
        );

        System.out.println(
            "Exchange successfully restored."
        );
    }

    // ================= DEMO DATA =================

    private static void createDemoExchange() {

        Stock tcs = new Stock(
            "TCS",
            "Tata Consultancy Services",
            new BigDecimal("3450")
        );

        exchange.registerStock(tcs);

        Portfolio buyerPortfolio =
            new Portfolio(
                new BigDecimal("500000")
            );

        Portfolio sellerPortfolio =
            new Portfolio(BigDecimal.ZERO);

        sellerPortfolio.addHolding(
            "TCS",
            100,
            new BigDecimal("3200")
        );

        Trader buyer = new Trader(
            "Demo Buyer",
            "buyer@algotrade.com",
            buyerPortfolio
        );

        Trader seller = new Trader(
            "Demo Seller",
            "seller@algotrade.com",
            sellerPortfolio
        );

        exchange.registerTrader(buyer);
        exchange.registerTrader(seller);
    }

    // ================= ACCOUNT SELECTION =================

    private static void selectAccount() {

        List<Trader> traders =
            exchange.getAllTraders();

        if (traders.isEmpty()) {

            System.out.println(
                "No trading accounts available."
            );

            return;
        }

        System.out.println(
            "\n========== SELECT ACCOUNT =========="
        );

        for (int i = 0; i < traders.size(); i++) {

            System.out.println(
                (i + 1) + ". "
                + traders.get(i).getName()
            );
        }

        int choice = readInt("Choose account: ");

        if (choice < 1 || choice > traders.size()) {

            System.out.println("Invalid account.");

            return;
        }

        currentTrader = traders.get(choice - 1);

        System.out.println(
            "Logged in as: " + currentTrader.getName()
        );
    }

    // ================= MARKET WATCH =================

    private static void marketWatch() {

        System.out.println(
            "\n========== MARKET WATCH =========="
        );

        System.out.printf(
            "%-12s %-30s %15s%n",
            "SYMBOL",
            "COMPANY",
            "PRICE"
        );

        Collection<Stock> stocks =
            exchange.getAllStocks();

        for (Stock stock : stocks) {

            System.out.printf(
                "%-12s %-30s ₹%14s%n",
                stock.getSymbol(),
                stock.getCompanyName(),
                stock.getMarketPrice()
            );
        }

        System.out.println(
            "=================================="
        );
    }

    // ================= PLACE ORDER =================

    private static void placeOrder() {

        if (currentTrader == null) {

            System.out.println(
                "Please select an account first."
            );

            return;
        }

        System.out.println("\n1. BUY");
        System.out.println("2. SELL");

        int sideChoice = readInt("Select side: ");

        OrderSide side;

        if (sideChoice == 1) {

            side = OrderSide.BUY;

        } else if (sideChoice == 2) {

            side = OrderSide.SELL;

        } else {

            System.out.println("Invalid side.");

            return;
        }

        System.out.println("\nAvailable stocks:");

        for (Stock stock : exchange.getAllStocks()) {
            System.out.println(stock.getSymbol());
        }

        System.out.print("Stock symbol: ");

        String symbol = scanner.nextLine()
            .trim()
            .toUpperCase();

        BigDecimal price =
            readDecimal("Limit price: ₹");

        int quantity =
            readInt("Quantity: ");

        try {

            Order order = new Order(
                currentTrader.getTraderId(),
                symbol,
                side,
                OrderType.LIMIT,
                price,
                quantity
            );

            List<Trade> trades =
                exchange.submitOrder(order);

            System.out.println("\nOrder submitted!");

            System.out.println(
                "Order ID: " + order.getOrderId()
            );

            System.out.println(
                "Status: " + order.getStatus()
            );

            System.out.println(
                "Executed Quantity: "
                + order.getExecutedQuantity()
            );

            System.out.println(
                "Trades Executed: " + trades.size()
            );

            for (Trade trade : trades) {
                System.out.println(trade);
            }

        } catch (RuntimeException e) {

            System.out.println(
                "Order rejected: " + e.getMessage()
            );
        }
    }

    // ================= VIEW PORTFOLIO =================

    private static void viewPortfolio() {

        if (currentTrader == null) {

            System.out.println(
                "Please select an account first."
            );

            return;
        }

        Portfolio portfolio =
            currentTrader.getPortfolio();

        System.out.println(
            "\n========== PORTFOLIO =========="
        );

        System.out.println(
            "Trader: " + currentTrader.getName()
        );

        System.out.println(
            "Cash Balance: ₹"
            + portfolio.getCashBalance()
        );

        System.out.println("\nHOLDINGS");

        portfolio.getHoldings().forEach(
            (symbol, holding) -> {

                System.out.println(
                    symbol
                    + " | Quantity: "
                    + holding.getQuantity()
                    + " | Average Price: ₹"
                    + holding.getAveragePurchasePrice()
                );
            }
        );

        System.out.println(
            "================================"
        );
    }

    // ================= TRADE HISTORY =================

    private static void tradeHistory() {

        List<Trade> trades =
            exchange.getTradeHistory();

        System.out.println(
            "\n========== TRADE HISTORY =========="
        );

        if (trades.isEmpty()) {

            System.out.println(
                "No trades executed yet."
            );

            return;
        }

        for (Trade trade : trades) {
            System.out.println(trade);
        }

        System.out.println(
            "Total Trades: " + trades.size()
        );

        System.out.println(
            "==================================="
        );
    }

    // ================= SAVE DATA =================

    private static void saveApplicationData() {

        try {

            Files.createDirectories(DATA_DIRECTORY);

            StockRepository stockRepository =
                new StockRepository(
                    DATA_DIRECTORY.resolve("stocks.csv")
                );

            TraderRepository traderRepository =
                new TraderRepository(DATA_DIRECTORY);

            TradeRepository tradeRepository =
                new TradeRepository(
                    DATA_DIRECTORY.resolve("trades.csv")
                );

            OrderRepository orderRepository =
                new OrderRepository(
                    DATA_DIRECTORY.resolve("orders.csv")
                );

            stockRepository.saveAll(
                exchange.getAllStocks()
            );

            traderRepository.saveAll(
                exchange.getAllTraders()
            );

            tradeRepository.saveAll(
                exchange.getTradeHistory()
            );

            orderRepository.saveAll(
                exchange.getAllOrders()
            );

            System.out.println(
                "\nAll application data saved successfully."
            );

        } catch (IOException | RuntimeException e) {

            System.out.println(
                "Could not save application data: "
                + e.getMessage()
            );
        }
    }

    // ================= INPUT HELPERS =================

    private static int readInt(String message) {

        while (true) {

            try {

                System.out.print(message);

                return Integer.parseInt(
                    scanner.nextLine().trim()
                );

            } catch (NumberFormatException e) {

                System.out.println(
                    "Enter a valid integer."
                );
            }
        }
    }

    private static BigDecimal readDecimal(String message) {

        while (true) {

            try {

                System.out.print(message);

                BigDecimal value = new BigDecimal(
                    scanner.nextLine().trim()
                );

                if (value.signum() <= 0) {

                    System.out.println(
                        "Value must be positive."
                    );

                    continue;
                }

                return value;

            } catch (NumberFormatException e) {

                System.out.println(
                    "Enter a valid amount."
                );
            }
        }
    }
}
