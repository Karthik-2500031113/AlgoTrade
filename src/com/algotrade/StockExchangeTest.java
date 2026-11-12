
package com.algotrade;

import java.math.BigDecimal;

import com.algotrade.exchange.StockExchange;
import com.algotrade.model.Stock;

public class StockExchangeTest {

    public static void main(String[] args) {

        StockExchange exchange = new StockExchange();

        // Register stocks
        exchange.registerStock(new Stock(
            "TCS",
            "Tata Consultancy Services",
            new BigDecimal("3450.00")
        ));

        exchange.registerStock(new Stock(
            "INFY",
            "Infosys",
            new BigDecimal("1450.00")
        ));

        exchange.registerStock(new Stock(
            "RELIANCE",
            "Reliance Industries",
            new BigDecimal("1200.00")
        ));

        exchange.registerStock(new Stock(
            "HDFCBANK",
            "HDFC Bank",
            new BigDecimal("1700.00")
        ));

        // Display exchange details
        System.out.println("===== ALGOTRADE EXCHANGE =====");

        System.out.println(
            "Exchange ID: " + exchange.getExchangeId()
        );

        System.out.println(
            "Total Stocks: " + exchange.getStockCount()
        );

        // Search stock
        Stock tcs = exchange.requireStock("tcs");

        System.out.println("\nStock Search:");

        System.out.println(
            tcs.getCompanyName() + " : " +
            tcs.getMarketPrice()
        );

        // Check stock existence
        System.out.println("\nStock Exists: " +
            exchange.containsStock("INFY"));

        // Display all stocks
        System.out.println("\nListed Stocks:");

        for (Stock stock : exchange.getAllStocks()) {

            System.out.println(
                stock.getSymbol() + " | " +
                stock.getCompanyName() + " | ₹" +
                stock.getMarketPrice()
            );
        }
    }
}
