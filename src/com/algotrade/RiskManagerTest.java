
package com.algotrade;

import java.math.BigDecimal;

import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderType;
import com.algotrade.exchange.RiskManager;
import com.algotrade.model.Order;
import com.algotrade.model.Portfolio;
import com.algotrade.model.Stock;
import com.algotrade.model.Trader;

public class RiskManagerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {

        RiskManager riskManager = new RiskManager(
            10000, new BigDecimal("1000000"));

        Stock stock = new Stock(
            "TCS",
            "Tata Consultancy Services",
            new BigDecimal("100"));

        Portfolio portfolio =
            new Portfolio(new BigDecimal("50000"));

        portfolio.addHolding(
            "TCS", 100, new BigDecimal("80"));

        Trader trader = new Trader(
            "Test Trader",
            "test@example.com",
            portfolio);

        System.out.println("===== ALGOTRADE RISK TEST =====");

        // Test 1: Valid buy
        test("Valid BUY", () -> {
            Order order = createOrder(
                trader, OrderSide.BUY, "100", 10);
            riskManager.validateOrder(order, trader, stock);
        }, false);

        // Test 2: Insufficient cash
        test("Insufficient Cash", () -> {
            Order order = createOrder(
                trader, OrderSide.BUY, "1000", 100);
            riskManager.validateOrder(order, trader, stock);
        }, true);

        // Test 3: Valid sell
        test("Valid SELL", () -> {
            Order order = createOrder(
                trader, OrderSide.SELL, "100", 20);
            riskManager.validateOrder(order, trader, stock);
        }, false);

        // Test 4: Insufficient shares
        test("Insufficient Shares", () -> {
            Order order = createOrder(
                trader, OrderSide.SELL, "100", 200);
            riskManager.validateOrder(order, trader, stock);
        }, true);

        // Test 5: Quantity limit
        test("Quantity Limit", () -> {
            Order order = createOrder(
                trader, OrderSide.BUY, "100", 10001);
            riskManager.validateOrder(order, trader, stock);
        }, true);

        // Test 6: Order value limit
        test("Order Value Limit", () -> {
            Order order = createOrder(
                trader, OrderSide.BUY, "200", 6000);
            riskManager.validateOrder(order, trader, stock);
        }, true);

        System.out.println("-------------------------------");
        System.out.println("PASSED: " + passed);
        System.out.println("FAILED: " + failed);

        if (failed > 0) {
            throw new AssertionError(
                failed + " risk tests failed");
        }
    }

    private static Order createOrder(
            Trader trader,
            OrderSide side,
            String price,
            int quantity) {

        return new Order(
            trader.getTraderId(),
            "TCS",
            side,
            OrderType.LIMIT,
            new BigDecimal(price),
            quantity);
    }

    private static void test(
            String name,
            Runnable action,
            boolean shouldReject) {

        try {
            action.run();

            if (shouldReject) {
                failed++;
                System.out.println("FAIL: " + name);
            } else {
                passed++;
                System.out.println("PASS: " + name);
            }

        } catch (IllegalStateException
                | IllegalArgumentException e) {

            if (shouldReject) {
                passed++;
                System.out.println("PASS: " + name
                    + " [BLOCKED: " + e.getMessage() + "]");
            } else {
                failed++;
                System.out.println("FAIL: " + name
                    + " [" + e.getMessage() + "]");
            }
        }
    }
}
