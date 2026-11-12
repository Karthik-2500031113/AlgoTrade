
package com.algotrade;

import java.math.BigDecimal;

import com.algotrade.model.*;

public class TraderTest {

    public static void main(String[] args) {

        Portfolio portfolio = new Portfolio(
            new BigDecimal("100000")
        );

        Trader trader = new Trader(
            "Karthik",
            "karthik@example.com",
            portfolio
        );

        System.out.println("Trader: " + trader.getName());
        System.out.println("ID: " + trader.getTraderId());
        System.out.println("Email: " + trader.getEmail());

        System.out.println("Balance: " +
            trader.getPortfolio().getCashBalance());
    }
}
