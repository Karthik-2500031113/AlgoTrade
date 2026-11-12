
package com.algotrade.persistence;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.algotrade.model.Trader;
import com.algotrade.model.Portfolio;
import com.algotrade.model.Holding;

public class TraderRepository {

    private final Path tradersFile;
    private final Path holdingsFile;

    public TraderRepository(Path directory) {
        if (directory == null) {
            throw new IllegalArgumentException(
                "Directory is required");
        }

        tradersFile = directory.resolve("traders.csv");
        holdingsFile = directory.resolve("holdings.csv");
    }

    public void saveAll(Collection<Trader> traders)
            throws IOException {

        if (traders == null) {
            throw new IllegalArgumentException(
                "Traders are required");
        }

        List<String[]> traderRows = new ArrayList<>();
        List<String[]> holdingRows = new ArrayList<>();

        traderRows.add(new String[] {
            "traderId", "name", "email", "cashBalance"
        });

        holdingRows.add(new String[] {
            "traderId", "symbol", "quantity", "averagePrice"
        });

        Set<String> traderIds = new HashSet<>();

        for (Trader trader : traders) {
            if (trader == null) {
                throw new IllegalArgumentException(
                    "Trader cannot be null");
            }

            String id = trader.getTraderId();

            if (!traderIds.add(id)) {
                throw new IllegalArgumentException(
                    "Duplicate trader ID: " + id);
            }

            Portfolio portfolio = trader.getPortfolio();

            traderRows.add(new String[] {
                id,
                trader.getName(),
                trader.getEmail(),
                portfolio.getCashBalance().toPlainString()
            });

            for (Holding holding :
                    portfolio.getHoldings().values()) {

                holdingRows.add(new String[] {
                    id,
                    holding.getStockSymbol(),
                    String.valueOf(holding.getQuantity()),
                    holding.getAveragePurchasePrice().toPlainString()
                });
            }
        }

        CsvUtil.write(tradersFile, traderRows);
        CsvUtil.write(holdingsFile, holdingRows);
    }

    public List<Trader> loadAll() throws IOException {

        List<String[]> traderRows = CsvUtil.read(tradersFile);
        List<String[]> holdingRows = CsvUtil.read(holdingsFile);

        List<Trader> traders = new ArrayList<>();
        Map<String, Trader> traderMap = new HashMap<>();

        if (traderRows.isEmpty()) {
            return traders;
        }

        if (!java.util.Arrays.equals(
                traderRows.get(0),
                new String[] {
                    "traderId", "name", "email", "cashBalance"
                })) {
            throw new IOException("Invalid traders CSV header");
        }

        for (int i = 1; i < traderRows.size(); i++) {

            String[] row = traderRows.get(i);

            if (row.length != 4) {
                throw new IOException(
                    "Invalid trader record at row " + (i + 1));
            }

            try {
                String id = row[0];

                Portfolio portfolio =
                    new Portfolio(new BigDecimal(row[3]));

                Trader trader = new Trader(
                    id, row[1], row[2], portfolio);

                if (traderMap.putIfAbsent(id, trader) != null) {
                    throw new IOException(
                        "Duplicate trader ID: " + id);
                }

                traders.add(trader);

            } catch (IllegalArgumentException e) {
                throw new IOException(
                    "Invalid trader data at row " + (i + 1), e);
            }
        }

        if (holdingRows.isEmpty()) {
            return traders;
        }

        if (!java.util.Arrays.equals(
                holdingRows.get(0),
                new String[] {
                    "traderId", "symbol", "quantity", "averagePrice"
                })) {
            throw new IOException("Invalid holdings CSV header");
        }

        for (int i = 1; i < holdingRows.size(); i++) {

            String[] row = holdingRows.get(i);

            if (row.length != 4) {
                throw new IOException(
                    "Invalid holding record at row " + (i + 1));
            }

            try {
                Trader trader = traderMap.get(row[0]);

                if (trader == null) {
                    throw new IOException(
                        "Unknown trader in holdings row " + (i + 1));
                }

                int quantity = Integer.parseInt(row[2]);

                if (quantity <= 0) {
                    throw new IOException(
                        "Invalid holding quantity at row " + (i + 1));
                }

                trader.getPortfolio().addHolding(
                    row[1],
                    quantity,
                    new BigDecimal(row[3])
                );

            } catch (IllegalArgumentException e) {
                throw new IOException(
                    "Invalid holding data at row " + (i + 1), e);
            }
        }

        return traders;
    }
}
