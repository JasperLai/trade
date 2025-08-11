package com.example.trade.demo.domain.entity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

// -----------------------------
// 2. 核心组件 (参考你的设计，调整结构)
// -----------------------------

// 1. MarketDepthAggregator (按 Symbol 管理，存储完整深度)
public class MarketDepthAggregator {
    private final String symbol;
    private final Map<String, NavigableMap<BigDecimal, BigDecimal>> askDepth = new HashMap<>();
    private final Map<String, NavigableMap<BigDecimal, BigDecimal>> bidDepth = new HashMap<>();

    public MarketDepthAggregator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() { return symbol; }

    public void updateDepth(String provider, List<OrderBookLevel> askLevels, List<OrderBookLevel> bidLevels) {
        System.out.println("[Aggregator] 更新 " + symbol + " 的 " + provider + " 深度数据");
        askDepth.put(provider, buildDepthMap(askLevels));
        bidDepth.put(provider, buildDepthMap(bidLevels));
    }

    private NavigableMap<BigDecimal, BigDecimal> buildDepthMap(List<OrderBookLevel> levels) {
        NavigableMap<BigDecimal, BigDecimal> depth = new TreeMap<>();
        for (OrderBookLevel level : levels) {
            depth.put(level.getPrice(), level.getQuantity());
        }
        return depth;
    }

    public Optional<BigDecimal> getBestAsk() {
        return askDepth.values().stream()
                .flatMap(depth -> depth.keySet().stream())
                .min(BigDecimal::compareTo);
    }

    public Optional<BigDecimal> getBestBid() {
        return bidDepth.values().stream()
                .flatMap(depth -> depth.keySet().stream())
                .max(BigDecimal::compareTo);
    }

    // 暴露全部深度（只读），供复杂策略（如 VMAP/VWAP）计算聚合盘口
    public Map<String, NavigableMap<BigDecimal, BigDecimal>> getAllAskDepth() {
        return Collections.unmodifiableMap(askDepth);
    }

    public Map<String, NavigableMap<BigDecimal, BigDecimal>> getAllBidDepth() {
        return Collections.unmodifiableMap(bidDepth);
    }

    // 可选：获取快照等
    @Override
    public String toString() {
        Optional<BigDecimal> bestBid = getBestBid();
        Optional<BigDecimal> bestAsk = getBestAsk();
        return String.format("MarketDepthAggregator{symbol='%s', bestBid=%s, bestAsk=%s}",
                           symbol, bestBid.orElse(null), bestAsk.orElse(null));
    }
}

