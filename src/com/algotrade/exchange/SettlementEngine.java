
package com.algotrade.exchange;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import com.algotrade.model.Trade;
import com.algotrade.model.Trader;
import com.algotrade.model.Portfolio;
import com.algotrade.model.Holding;

public class SettlementEngine {

    private final Set<Trade> settledTrades =
        Collections.newSetFromMap(new IdentityHashMap<>());

    public synchronized void settleTrade(
            Trade trade, Trader buyer, Trader seller) {

        if (trade == null || buyer == null || seller == null) {
            throw new IllegalArgumentException(
                "Trade, buyer and seller are required");
        }

        if (!trade.getBuyerId().equals(buyer.getTraderId())
                || !trade.getSellerId().equals(seller.getTraderId())) {
            throw new IllegalArgumentException(
                "Trade participants do not match the supplied traders");
        }

        if (settledTrades.contains(trade)) {
            throw new IllegalStateException(
                "This trade has already been settled");
        }

        Portfolio buyerPortfolio = buyer.getPortfolio();
        Portfolio sellerPortfolio = seller.getPortfolio();

        BigDecimal tradeValue = trade.getTotalValue();
        String symbol = trade.getStockSymbol();
        int quantity = trade.getQuantity();

        // Validate buyer's cash.
        if (buyerPortfolio.getCashBalance().compareTo(tradeValue) < 0) {
            throw new IllegalStateException(
                "Buyer has insufficient cash");
        }

        // Validate seller's shares.
        Holding sellerHolding = sellerPortfolio.getHolding(symbol);

        if (sellerHolding == null
                || sellerHolding.getQuantity() < quantity) {
            throw new IllegalStateException(
                "Seller has insufficient shares");
        }

        // Apply settlement.
        buyerPortfolio.withdraw(tradeValue);
        sellerPortfolio.sellHolding(symbol, quantity);

        buyerPortfolio.addHolding(
            symbol, quantity, trade.getExecutionPrice());

        sellerPortfolio.deposit(tradeValue);

        settledTrades.add(trade);
    }

    public synchronized boolean isSettled(Trade trade) {
        return settledTrades.contains(trade);
    }

    public synchronized int getSettledTradeCount() {
        return settledTrades.size();
    }
}
