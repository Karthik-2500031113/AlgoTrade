
package com.algotrade;

import java.math.BigDecimal;

import com.algotrade.exchange.SettlementEngine;
import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderType;
import com.algotrade.model.Order;
import com.algotrade.model.Portfolio;
import com.algotrade.model.Trade;
import com.algotrade.model.Trader;

public class SettlementEngineTest {

    public static void main(String[] args) {

        // Create portfolios
        Portfolio buyerPortfolio =
            new Portfolio(new BigDecimal("5000"));

        Portfolio sellerPortfolio =
            new Portfolio(new BigDecimal("0"));

        // Give seller 20 TCS shares
        sellerPortfolio.addHolding(
            "TCS", 20, new BigDecimal("80"));

        // Create traders
        Trader buyer = new Trader(
            "Buyer", "buyer@example.com", buyerPortfolio);

        Trader seller = new Trader(
            "Seller", "seller@example.com", sellerPortfolio);

        // Create and activate orders
        Order buyOrder = new Order(
            buyer.getTraderId(), "TCS",
            OrderSide.BUY, OrderType.LIMIT,
            new BigDecimal("110"), 10);

        Order sellOrder = new Order(
            seller.getTraderId(), "TCS",
            OrderSide.SELL, OrderType.LIMIT,
            new BigDecimal("90"), 10);

        buyOrder.activate();
        sellOrder.activate();

        // Create trade at execution price ₹100
        Trade trade = new Trade(
            buyOrder, sellOrder,
            new BigDecimal("100"), 10);

        // Settle trade
        SettlementEngine settlementEngine =
            new SettlementEngine();

        settlementEngine.settleTrade(trade, buyer, seller);

        System.out.println("===== ALGOTRADE SETTLEMENT TEST =====");

        System.out.println("Buyer Cash: ₹"
            + buyerPortfolio.getCashBalance());

        System.out.println("Seller Cash: ₹"
            + sellerPortfolio.getCashBalance());

        System.out.println("Buyer TCS Shares: "
            + buyerPortfolio.getHolding("TCS").getQuantity());

        System.out.println("Seller TCS Shares: "
            + sellerPortfolio.getHolding("TCS").getQuantity());

        System.out.println("Trade Settled: "
            + settlementEngine.isSettled(trade));

        System.out.println("Settled Trade Count: "
            + settlementEngine.getSettledTradeCount());

        // Test duplicate settlement protection
        try {
            settlementEngine.settleTrade(trade, buyer, seller);
            System.out.println("ERROR: Duplicate settlement allowed!");
        } catch (IllegalStateException e) {
            System.out.println("Duplicate Settlement: BLOCKED");
        }
    }
}
