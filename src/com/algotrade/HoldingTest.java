
package com.algotrade;

import java.math.BigDecimal;

import com.algotrade.model.Holding;

public class HoldingTest {

    public static void main(String[] args) {

        Holding holding = new Holding(
            "TCS",
            10,
            new BigDecimal("100.00")
        );

        System.out.println("Initial Quantity: "
            + holding.getQuantity());

        System.out.println("Initial Average: "
            + holding.getAveragePurchasePrice());

        // Buy 20 more shares at 150
        holding.addShares(
            20,
            new BigDecimal("150.00")
        );

        System.out.println("\nAfter Purchase:");

        System.out.println("Quantity: "
            + holding.getQuantity());

        System.out.println("Average Price: "
            + holding.getAveragePurchasePrice());

        // Sell 10 shares
        holding.removeShares(10);

        System.out.println("\nAfter Selling:");

        System.out.println("Quantity: "
            + holding.getQuantity());

        System.out.println("Average Price: "
            + holding.getAveragePurchasePrice());
    }
}
