package com.example.trade.demo.domain.valueobject;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

/**
 * 市场深度快照值对象
 * 表示特定时刻的市场深度状态
 */
public class MarketDepthSnapshot {
    
    private final String symbol;
    private final NavigableMap<BigDecimal, BigDecimal> mergedAskDepth;
    private final NavigableMap<BigDecimal, BigDecimal> mergedBidDepth;
    private final long timestamp; // 使用毫秒时间戳，性能更好
    private final Map<String, NavigableMap<BigDecimal, BigDecimal>> originalAskDepth;
    private final Map<String, NavigableMap<BigDecimal, BigDecimal>> originalBidDepth;
    
    public MarketDepthSnapshot(String symbol, 
                              Map<String, NavigableMap<BigDecimal, BigDecimal>> originalAskDepth,
                              Map<String, NavigableMap<BigDecimal, BigDecimal>> originalBidDepth) {
        this.symbol = symbol;
        this.originalAskDepth = Collections.unmodifiableMap(originalAskDepth);
        this.originalBidDepth = Collections.unmodifiableMap(originalBidDepth);
        this.mergedAskDepth = mergeDepth(originalAskDepth);
        this.mergedBidDepth = mergeDepth(originalBidDepth);
        this.timestamp = System.currentTimeMillis();
    }
    
    // 合并各提供商的深度数据
    private NavigableMap<BigDecimal, BigDecimal> mergeDepth(Map<String, NavigableMap<BigDecimal, BigDecimal>> depthData) {
        NavigableMap<BigDecimal, BigDecimal> merged = new TreeMap<>();
        for (NavigableMap<BigDecimal, BigDecimal> providerDepth : depthData.values()) {
            for (Map.Entry<BigDecimal, BigDecimal> entry : providerDepth.entrySet()) {
                merged.merge(entry.getKey(), entry.getValue(), BigDecimal::add);
            }
        }
        return merged;
    }
    
    // 查询方法
    public Optional<BigDecimal> getBestBid() {
        return mergedBidDepth.isEmpty() ? Optional.empty() : Optional.of(mergedBidDepth.lastKey());
    }
    
    public Optional<BigDecimal> getBestAsk() {
        return mergedAskDepth.isEmpty() ? Optional.empty() : Optional.of(mergedAskDepth.firstKey());
    }
    
    public Optional<BigDecimal> getSpread() {
        Optional<BigDecimal> bestBid = getBestBid();
        Optional<BigDecimal> bestAsk = getBestAsk();
        if (bestBid.isPresent() && bestAsk.isPresent()) {
            return Optional.of(bestAsk.get().subtract(bestBid.get()));
        }
        return Optional.empty();
    }
    
    public boolean hasValidDepth() {
        return !mergedBidDepth.isEmpty() && !mergedAskDepth.isEmpty();
    }
    
    public boolean isStale(Duration maxAge) {
        return Duration.ofMillis(System.currentTimeMillis() - timestamp).compareTo(maxAge) > 0;
    }
    
    // 计算指定数量级别的累计数量
    public BigDecimal getCumulativeQuantity(int levels, boolean isBid) {
        NavigableMap<BigDecimal, BigDecimal> depth = isBid ? mergedBidDepth.descendingMap() : mergedAskDepth;
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        
        for (BigDecimal qty : depth.values()) {
            if (count++ >= levels) break;
            total = total.add(qty);
        }
        return total;
    }
    
    // 获取指定价格级别的数量
    public BigDecimal getQuantityAtPrice(BigDecimal price, boolean isBid) {
        NavigableMap<BigDecimal, BigDecimal> depth = isBid ? mergedBidDepth : mergedAskDepth;
        return depth.getOrDefault(price, BigDecimal.ZERO);
    }
    
    // Getter方法
    public String getSymbol() { return symbol; }
    public long getTimestamp() { return timestamp; }
    public NavigableMap<BigDecimal, BigDecimal> getMergedAskDepth() { 
        return mergedAskDepth; 
    }
    public NavigableMap<BigDecimal, BigDecimal> getMergedBidDepth() { 
        return mergedBidDepth; 
    }
    public Map<String, NavigableMap<BigDecimal, BigDecimal>> getOriginalAskDepth() { 
        return originalAskDepth; 
    }
    public Map<String, NavigableMap<BigDecimal, BigDecimal>> getOriginalBidDepth() { 
        return originalBidDepth; 
    }
    
    @Override
    public String toString() {
        Optional<BigDecimal> bestBid = getBestBid();
        Optional<BigDecimal> bestAsk = getBestAsk();
        return String.format("MarketDepthSnapshot{symbol='%s', bestBid=%s, bestAsk=%s, spread=%s, timestamp=%d}",
                           symbol, 
                           bestBid.map(BigDecimal::toString).orElse("null"),
                           bestAsk.map(BigDecimal::toString).orElse("null"),
                           getSpread().map(BigDecimal::toString).orElse("null"),
                           timestamp);
    }
}
