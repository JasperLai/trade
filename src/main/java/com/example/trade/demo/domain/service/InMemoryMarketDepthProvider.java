package com.example.trade.demo.domain.service;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.trade.demo.domain.entity.MarketDepthAggregator;
import com.example.trade.demo.domain.valueobject.MarketDepthSnapshot;

/**
 * 内存实现的市场深度提供者
 * 连接到同一JVM中的聚合器
 */
@Component
public class InMemoryMarketDepthProvider implements MarketDepthProvider {
    
    private final Map<String, MarketDepthAggregator> aggregators = new ConcurrentHashMap<>();
    
    public InMemoryMarketDepthProvider() {
        // 初始化一些测试数据
        initializeTestData();
    }
    
    @Override
    public Optional<MarketDepthSnapshot> getDepth(String symbol) {
        return getDepth(symbol, Duration.ofSeconds(5)); // 默认5秒过期时间
    }
    
    @Override
    public Optional<MarketDepthSnapshot> getDepth(String symbol, Duration maxAge) {
        MarketDepthAggregator aggregator = aggregators.get(symbol);
        if (aggregator == null) {
            System.out.println("[InMemoryProvider] 未找到聚合器: " + symbol);
            return Optional.empty();
        }
        
        if (aggregator.isDataStale(maxAge)) {
            System.out.println("[InMemoryProvider] 数据已过期: " + symbol);
            return Optional.empty();
        }
        
        return Optional.of(aggregator.getSnapshot());
    }
    
    @Override
    public boolean isSymbolSupported(String symbol) {
        return aggregators.containsKey(symbol);
    }
    
    @Override
    public List<String> getSupportedSymbols() {
        return List.copyOf(aggregators.keySet());
    }
    
    @Override
    public long getLastUpdateTime(String symbol) {
        MarketDepthAggregator aggregator = aggregators.get(symbol);
        return aggregator != null ? aggregator.getLastUpdateTime() : -1;
    }
    
    /**
     * 添加或更新聚合器
     * @param symbol 交易对符号
     * @param aggregator 聚合器
     */
    public void addAggregator(String symbol, MarketDepthAggregator aggregator) {
        aggregators.put(symbol, aggregator);
        System.out.println("[InMemoryProvider] 添加聚合器: " + symbol);
    }
    
    /**
     * 移除聚合器
     * @param symbol 交易对符号
     */
    public void removeAggregator(String symbol) {
        aggregators.remove(symbol);
        System.out.println("[InMemoryProvider] 移除聚合器: " + symbol);
    }
    
    /**
     * 获取聚合器（用于测试）
     * @param symbol 交易对符号
     * @return 聚合器
     */
    public MarketDepthAggregator getAggregator(String symbol) {
        return aggregators.get(symbol);
    }
    
    /**
     * 初始化测试数据
     */
    private void initializeTestData() {
        // 创建BTCUSDT聚合器
        MarketDepthAggregator btcAggregator = new MarketDepthAggregator("BTCUSDT");
        aggregators.put("BTCUSDT", btcAggregator);
        
        // 创建ETHUSDT聚合器
        MarketDepthAggregator ethAggregator = new MarketDepthAggregator("ETHUSDT");
        aggregators.put("ETHUSDT", ethAggregator);
        
        System.out.println("[InMemoryProvider] 初始化测试数据，支持的交易对: " + Arrays.toString(getSupportedSymbols().toArray()));
    }
}
