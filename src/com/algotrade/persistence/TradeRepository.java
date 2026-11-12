
package com.algotrade.persistence;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.algotrade.model.Trade;

public class TradeRepository {

    private final Path filePath;

    private static final String[] HEADER = {
        "tradeId",
        "buyOrderId",
        "sellOrderId",
        "buyerId",
        "sellerId",
        "symbol",
        "price",
        "quantity",
        "executedAt"
    };

    public TradeRepository(Path filePath) {

        if (filePath == null) {
            throw new IllegalArgumentException(
                "File path is required"
            );
        }

        this.filePath = filePath;
    }

    // ================= SAVE TRADES =================

    public void saveAll(List<Trade> trades)
            throws IOException {

        if (trades == null) {
            throw new IllegalArgumentException(
                "Trade list is required"
            );
        }

        List<String[]> rows = new ArrayList<>();

        rows.add(HEADER.clone());

        for (Trade trade : trades) {

            if (trade == null) {
                throw new IllegalArgumentException(
                    "Trade cannot be null"
                );
            }

            rows.add(new String[] {

                trade.getTradeId(),
                trade.getBuyOrderId(),
                trade.getSellOrderId(),
                trade.getBuyerId(),
                trade.getSellerId(),
                trade.getStockSymbol(),
                trade.getExecutionPrice().toPlainString(),
                String.valueOf(trade.getQuantity()),
                trade.getExecutedAt().toString()

            });
        }

        CsvUtil.write(filePath, rows);
    }

    // ================= LOAD RAW RECORDS =================

    public List<String[]> loadAll() throws IOException {

        List<String[]> rows = CsvUtil.read(filePath);

        if (rows.isEmpty()) {
            return rows;
        }

        if (!Arrays.equals(rows.get(0), HEADER)) {
            throw new IOException(
                "Invalid trade CSV header"
            );
        }

        for (int i = 1; i < rows.size(); i++) {

            String[] row = rows.get(i);

            if (row.length != 9) {
                throw new IOException(
                    "Invalid trade record at row " + (i + 1)
                );
            }

            try {

                for (int j = 0; j < 6; j++) {

                    if (row[j] == null || row[j].isBlank()) {
                        throw new IllegalArgumentException(
                            "Missing trade field"
                        );
                    }
                }

                BigDecimal price = new BigDecimal(row[6]);

                if (price.signum() <= 0) {
                    throw new IllegalArgumentException(
                        "Invalid execution price"
                    );
                }

                int quantity = Integer.parseInt(row[7]);

                if (quantity <= 0) {
                    throw new IllegalArgumentException(
                        "Invalid trade quantity"
                    );
                }

                Instant.parse(row[8]);

            } catch (RuntimeException e) {

                throw new IOException(
                    "Invalid trade data at CSV row " + (i + 1),
                    e
                );
            }
        }

        return rows;
    }

    // ================= RESTORE TRADE OBJECTS =================

    public List<Trade> loadTrades() throws IOException {

        List<String[]> rows = loadAll();

        List<Trade> trades = new ArrayList<>();

        if (rows.isEmpty()) {
            return trades;
        }

        for (int i = 1; i < rows.size(); i++) {

            String[] row = rows.get(i);

            try {

                Trade trade = new Trade(
                    row[0],
                    row[1],
                    row[2],
                    row[3],
                    row[4],
                    row[5],
                    new BigDecimal(row[6]),
                    Integer.parseInt(row[7]),
                    Instant.parse(row[8])
                );

                trades.add(trade);

            } catch (RuntimeException e) {

                throw new IOException(
                    "Could not restore trade at CSV row " + (i + 1),
                    e
                );
            }
        }

        return List.copyOf(trades);
    }

    // ================= TRADE COUNT =================

    public int getTradeCount() throws IOException {

        return loadTrades().size();
    }
}
