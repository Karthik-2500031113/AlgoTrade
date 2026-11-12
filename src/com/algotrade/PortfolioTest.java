
package com.algotrade;

import java.math.BigDecimal;
import java.util.Map;

import com.algotrade.model.Portfolio;

public class PortfolioTest {

    public static void main(String[] args) {

        Portfolio portfolio = new Portfolio(
            new BigDecimal("100000")
        );

        portfolio.deposit(new BigDecimal("5000"));

        portfolio.addHolding(
            "TCS", 10, new BigDecimal("3400")
        );

        portfolio.addHolding(
            "INFY", 5, new BigDecimal("1400")
        );

        Map<String, BigDecimal> prices = Map.of(
            "TCS", new BigDecimal("3500"),
            "INFY", new BigDecimal("1500")
        );

        System.out.println("Cash: "
            + portfolio.getCashBalance());

        System.out.println("Holdings: "
            + portfolio.getHoldings().size());

        System.out.println("Market Value: "
            + portfolio.calculateMarketValue(prices));

        System.out.println("Total Portfolio Value: "
            + portfolio.calculateTotalValue(prices));

        portfolio.withdraw(new BigDecimal("1000"));

        System.out.println("After Withdrawal: "
            + portfolio.getCashBalance());
    }
}
