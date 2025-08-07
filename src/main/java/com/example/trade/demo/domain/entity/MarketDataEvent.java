package com.example.trade.demo.domain.entity;

import java.util.Collections;
import java.util.List;

public class MarketDataEvent {
    private final String symbol;
    private final String provider;
    private final List<OrderBookLevel> askLevels;
    private final List<OrderBookLevel> bidLevels;

    public MarketDataEvent(String symbol, String provider,
                           List<OrderBookLevel> askLevels,
                           List<OrderBookLevel> bidLevels) {
        this.symbol = symbol;
        this.provider = provider;
        this.askLevels = askLevels != null ? askLevels : Collections.emptyList();
        this.bidLevels = bidLevels != null ? bidLevels : Collections.emptyList();
    }

    public String getSymbol() { return symbol; }
    public String getProvider() { return provider; }
    public List<OrderBookLevel> getAskLevels() { return askLevels; }
    public List<OrderBookLevel> getBidLevels() { return bidLevels; }

    @Override
    public String toString() {
        return String.format("MarketDataEvent{symbol='%s', provider='%s', asks=%d, bids=%d}",
                           symbol, provider, askLevels.size(), bidLevels.size());
    }
}

