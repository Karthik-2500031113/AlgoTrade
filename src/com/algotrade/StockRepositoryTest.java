
package com.algotrade;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.algotrade.model.Stock;
import com.algotrade.persistence.StockRepository;

public class StockRepositoryTest {

    public static void main(String[] args) throws Exception {

        StockRepository repository =
            new StockRepository(
                Path.of("data", "stocks.csv"));

        List<Stock> original = new ArrayList<>();

        original.add(new Stock(
            "TCS", "Tata Consultancy Services",
            new BigDecimal("3450")));

        original.add(new Stock(
            "INFY", "Infosys Limited",
            new BigDecimal("1800")));

        original.add(new Stock(
            "RELIANCE", "Reliance Industries",
            new BigDecimal("1250")));

        // Save stocks
        repository.saveAll(original);

        // Load stocks
        List<Stock> loaded = repository.loadAll();

        System.out.println("===== STOCK REPOSITORY TEST =====");

        System.out.println("Stocks Saved: " + original.size());
        System.out.println("Stocks Loaded: " + loaded.size());

        for (Stock stock : loaded) {
            System.out.println(
                stock.getSymbol() + " | "
                + stock.getCompanyName() + " | ₹"
                + stock.getMarketPrice());
        }

        boolean passed = loaded.size() == original.size();

        for (int i = 0; passed && i < original.size(); i++) {
            Stock a = original.get(i);
            Stock b = loaded.get(i);

            passed =
                a.getSymbol().equals(b.getSymbol())
                && a.getCompanyName().equals(b.getCompanyName())
                && a.getMarketPrice().compareTo(
                    b.getMarketPrice()) == 0
                && a.getPreviousClose().compareTo(
                    b.getPreviousClose()) == 0;
        }

        System.out.println("Stock Persistence: "
            + (passed ? "PASSED" : "FAILED"));

        if (!passed) {
            throw new AssertionError(
                "Stock persistence test failed");
        }
    }
}
