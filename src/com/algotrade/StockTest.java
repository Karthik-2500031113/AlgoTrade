
package com.algotrade;

import java.math.BigDecimal;

import com.algotrade.model.Stock;

public class StockTest {

    public static void main(String[] args) {

        Stock tcs = new Stock(
            "TCS",
            "Tata Consultancy Services",
            new BigDecimal("3450.00")
        );

        System.out.println("Stock: " + tcs.getSymbol());

        System.out.println(
            "Company: " + tcs.getCompanyName()
        );

        System.out.println(
            "Market Price: " + tcs.getMarketPrice()
        );

        tcs.recordTrade(
            new BigDecimal("3500.00"),
            100
        );

        System.out.println(
            "Updated Price: " + tcs.getMarketPrice()
        );

        System.out.println(
            "Total Traded Quantity: " +
            tcs.getTotalTradedQuantity()
        );
    }
}
