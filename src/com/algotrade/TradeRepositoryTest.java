
package com.algotrade;

import java.nio.file.Path;
import java.util.List;

import com.algotrade.model.Trade;
import com.algotrade.persistence.TradeRepository;

public class TradeRepositoryTest {

    public static void main(String[] args) throws Exception {

        TradeRepository repository =
            new TradeRepository(
                Path.of("data", "trades.csv")
            );

        // Load saved trades
        List<Trade> trades = repository.loadTrades();

        System.out.println(
            "===== TRADE RESTORATION TEST ====="
        );

        System.out.println(
            "Restored Trade Count: " + trades.size()
        );

        for (Trade trade : trades) {

            System.out.println("\n" + trade);

            System.out.println(
                "Trade ID: " + trade.getTradeId()
            );

            System.out.println(
                "Timestamp: " + trade.getExecutedAt()
            );
        }

        System.out.println(
            "\nTrade Restoration: "
            + (trades.size() == repository.getTradeCount()
                ? "PASSED" : "FAILED")
        );
    }
}
