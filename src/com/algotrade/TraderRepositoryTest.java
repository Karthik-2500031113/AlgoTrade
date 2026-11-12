
package com.algotrade;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import com.algotrade.model.Portfolio;
import com.algotrade.model.Trader;
import com.algotrade.persistence.TraderRepository;

public class TraderRepositoryTest {

    public static void main(String[] args) throws Exception {

        TraderRepository repository =
            new TraderRepository(Path.of("data"));

        Portfolio portfolio =
            new Portfolio(new BigDecimal("50000"));

        portfolio.addHolding(
            "TCS", 20, new BigDecimal("3450"));

        portfolio.addHolding(
            "INFY", 10, new BigDecimal("1800"));

        Trader trader = new Trader(
            "Test Trader",
            "test@example.com",
            portfolio);

        // Save
        repository.saveAll(List.of(trader));

        // Restore
        List<Trader> loaded = repository.loadAll();

        Trader restored = loaded.get(0);

        System.out.println("===== TRADER REPOSITORY TEST =====");
        System.out.println("Traders Loaded: " + loaded.size());
        System.out.println("ID Preserved: "
            + trader.getTraderId().equals(restored.getTraderId()));
        System.out.println("Name: " + restored.getName());
        System.out.println("Email: " + restored.getEmail());
        System.out.println("Cash: ₹"
            + restored.getPortfolio().getCashBalance());
        System.out.println("TCS Quantity: "
            + restored.getPortfolio().getHolding("TCS").getQuantity());
        System.out.println("INFY Quantity: "
            + restored.getPortfolio().getHolding("INFY").getQuantity());

        boolean passed =
            trader.getTraderId().equals(restored.getTraderId())
            && restored.getPortfolio().getCashBalance()
                .compareTo(new BigDecimal("50000")) == 0
            && restored.getPortfolio().getHolding("TCS").getQuantity() == 20
            && restored.getPortfolio().getHolding("INFY").getQuantity() == 10;

        System.out.println("Trader Persistence: "
            + (passed ? "PASSED" : "FAILED"));

        if (!passed) {
            throw new AssertionError("Trader persistence failed");
        }
    }
}
