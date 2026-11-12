
package com.algotrade;

import java.math.BigDecimal;
import java.util.List;

import com.algotrade.exchange.ExchangeManager;
import com.algotrade.enums.OrderSide;
import com.algotrade.enums.OrderType;
import com.algotrade.model.Order;
import com.algotrade.model.Portfolio;
import com.algotrade.model.Stock;
import com.algotrade.model.Trader;
import com.algotrade.model.Trade;

public class ExchangeManagerTest {

    public static void main(String[] args) {

        ExchangeManager exchange = new ExchangeManager();

        // Create stock
        Stock tcs = new Stock(
            "TCS",
            "Tata Consultancy Services",
            new BigDecimal("100")
        );

        exchange.registerStock(tcs);

        // Create portfolios
        Portfolio buyerPortfolio =
            new Portfolio(new BigDecimal("5000"));

        Portfolio sellerPortfolio =
            new Portfolio(new BigDecimal("0"));

        sellerPortfolio.addHolding(
            "TCS", 20, new BigDecimal("80"));

        // Create traders
        Trader buyer = new Trader(
            "Buyer", "buyer@example.com", buyerPortfolio);

        Trader seller = new Trader(
            "Seller", "seller@example.com", sellerPortfolio);

        exchange.registerTrader(buyer);
        exchange.registerTrader(seller);

        // Seller places a limit order
        Order sellOrder = new Order(
            seller.getTraderId(),
            "TCS",
            OrderSide.SELL,
            OrderType.LIMIT,
            new BigDecimal("100"),
            10
        );

        exchange.submitOrder(sellOrder);

        // Buyer places a matching limit order
        Order buyOrder = new Order(
            buyer.getTraderId(),
            "TCS",
            OrderSide.BUY,
            OrderType.LIMIT,
            new BigDecimal("105"),
            10
        );

        List<Trade> trades = exchange.submitOrder(buyOrder);

        // Display results
        System.out.println("===== ALGOTRADE EXCHANGE TEST =====");

        System.out.println("Registered Stocks: "
            + exchange.getRegisteredStockCount());

        System.out.println("Registered Traders: "
            + exchange.getTraderCount());

        System.out.println("Executed Trades: " + trades.size());

        System.out.println("Execution Price: "
            + trades.get(0).getExecutionPrice());

        System.out.println("Execution Quantity: "
            + trades.get(0).getQuantity());

        System.out.println("Buyer Cash: ₹"
            + buyerPortfolio.getCashBalance());

        System.out.println("Seller Cash: ₹"
            + sellerPortfolio.getCashBalance());

        System.out.println("Buyer Shares: "
            + buyerPortfolio.getHolding("TCS").getQuantity());

        System.out.println("Seller Shares: "
            + sellerPortfolio.getHolding("TCS").getQuantity());

        System.out.println("Trade History: "
            + exchange.getTradeHistory().size());

        System.out.println("Active Orders: "
            + exchange.getOrderBook("TCS").getActiveOrderCount());
    }
}
