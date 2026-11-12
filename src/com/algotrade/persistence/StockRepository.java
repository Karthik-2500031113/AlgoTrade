
package com.algotrade.persistence;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.algotrade.model.Stock;

public class StockRepository {

    private final Path filePath;

    public StockRepository(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException(
                "File path is required");
        }

        this.filePath = filePath;
    }

    // Save all stocks
    public void saveAll(Collection<Stock> stocks)
            throws IOException {

        if (stocks == null) {
            throw new IllegalArgumentException(
                "Stock collection is required");
        }

        List<String[]> rows = new ArrayList<>();

        rows.add(new String[] {
            "symbol",
            "companyName",
            "marketPrice",
            "previousClose"
        });

        Set<String> symbols = new HashSet<>();

        for (Stock stock : stocks) {
            if (stock == null) {
                throw new IllegalArgumentException(
                    "Stock cannot be null");
            }

            if (!symbols.add(stock.getSymbol())) {
                throw new IllegalArgumentException(
                    "Duplicate stock: " + stock.getSymbol());
            }

            rows.add(new String[] {
                stock.getSymbol(),
                stock.getCompanyName(),
                stock.getMarketPrice().toPlainString(),
                stock.getPreviousClose().toPlainString()
            });
        }

        CsvUtil.write(filePath, rows);
    }

    // Load all stocks
    public List<Stock> loadAll() throws IOException {

        List<String[]> rows = CsvUtil.read(filePath);
        List<Stock> stocks = new ArrayList<>();

        if (rows.isEmpty()) {
            return stocks;
        }

        String[] header = {
            "symbol",
            "companyName",
            "marketPrice",
            "previousClose"
        };

        if (!java.util.Arrays.equals(rows.get(0), header)) {
            throw new IOException("Invalid stock CSV header");
        }

        Set<String> symbols = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {

            String[] row = rows.get(i);

            if (row.length != 4) {
                throw new IOException(
                    "Invalid stock record at CSV row " + (i + 1));
            }

            try {
                String symbol = row[0].trim();

                if (symbol.isEmpty() || !symbols.add(symbol)) {
                    throw new IOException(
                        "Empty or duplicate stock symbol at row "
                        + (i + 1));
                }

                Stock stock = new Stock(
                    symbol,
                    row[1],
                    new BigDecimal(row[2])
                );

                stocks.add(stock);

            } catch (NumberFormatException e) {
                throw new IOException(
                    "Invalid price at CSV row " + (i + 1), e);
            }
        }

        return stocks;
    }
}
