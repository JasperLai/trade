package com.example.trade.demo.domain.entity;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import com.example.trade.demo.domain.valueobject.MarketDepthSnapshot;

// -----------------------------
// 2. 核心组件 (参考你的设计，调整结构)
// -----------------------------

// 1. MarketDepthAggregator (按 Symbol 管理，存储完整深度) - 聚合根
public class MarketDepthAggregator {
    private final String symbol;
    private final Map<String, NavigableMap<BigDecimal, BigDecimal>> askDepth = new HashMap<>();
    private final Map<String, NavigableMap<BigDecimal, BigDecimal>> bidDepth = new HashMap<>();
    private final AtomicLong lastUpdateTimestamp = new AtomicLong(System.currentTimeMillis());

    public MarketDepthAggregator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() { return symbol; }

    public void updateDepth(String provider, List<OrderBookLevel> askLevels, List<OrderBookLevel> bidLevels) {
        System.out.println("[Aggregator] 更新 " + symbol + " 的 " + provider + " 深度数据");
        askDepth.put(provider, buildDepthMap(askLevels));
        bidDepth.put(provider, buildDepthMap(bidLevels));
        lastUpdateTimestamp.set(System.currentTimeMillis()); // 更新时间戳
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

    // === 增强的聚合方法 ===
    
    /**
     * 生成当前市场深度快照
     * @return 市场深度快照
     */
    public MarketDepthSnapshot getSnapshot() {
        return new MarketDepthSnapshot(symbol, askDepth, bidDepth);
    }
    
    /**
     * 检查数据是否过期
     * @param maxAge 最大允许的数据年龄
     * @return 如果数据过期返回true
     */
    public boolean isDataStale(Duration maxAge) {
        long age = System.currentTimeMillis() - lastUpdateTimestamp.get();
        return Duration.ofMillis(age).compareTo(maxAge) > 0;
    }
    
    /**
     * 检查数据是否新鲜（默认1秒）
     * @return 如果数据新鲜返回true
     */
    public boolean isDataFresh() {
        return isDataStale(Duration.ofSeconds(1));
    }
    
    /**
     * 获取最后更新时间
     * @return 最后更新的毫秒时间戳
     */
    public long getLastUpdateTime() {
        return lastUpdateTimestamp.get();
    }
    
    /**
     * 清理过期的深度数据（可以定期调用）
     * @param maxProviders 最大保留的提供商数量
     */
    public void purgeOldData(int maxProviders) {
        // 简单的LRU清理策略：如果提供商过多，清理最旧的
        // 这里简化实现，实际可以根据业务需求调整
        if (askDepth.size() > maxProviders) {
            System.out.println("[Aggregator] 清理过期数据，当前提供商数量: " + askDepth.size());
        }
    }
    
    /**
     * 检查是否有有效的深度数据
     * @return 如果有有效数据返回true
     */
    public boolean hasValidDepth() {
        return !askDepth.isEmpty() && !bidDepth.isEmpty() && 
               getBestBid().isPresent() && getBestAsk().isPresent();
    }
    
    // 可选：获取快照等
    @Override
    public String toString() {
        Optional<BigDecimal> bestBid = getBestBid();
        Optional<BigDecimal> bestAsk = getBestAsk();
        return String.format("MarketDepthAggregator{symbol='%s', bestBid=%s, bestAsk=%s, lastUpdate=%d}",
                           symbol, bestBid.orElse(null), bestAsk.orElse(null), lastUpdateTimestamp.get());
    }
}
