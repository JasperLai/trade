package com.example.trade.demo.domain.service;
import com.example.trade.demo.domain.valueobject.MarketDepthSnapshot;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 市场深度提供者接口
 * 连接独立运行的订单簿服务，获取市场深度数据
 */
public interface MarketDepthProvider {
    
    /**
     * 获取指定交易对的当前市场深度快照
     * @param symbol 交易对符号
     * @return 市场深度快照，如果数据不可用返回Optional.empty()
     */
    Optional<MarketDepthSnapshot> getDepth(String symbol);
    
    /**
     * 获取指定交易对的市场深度快照，如果数据过期则返回空
     * @param symbol 交易对符号
     * @param maxAge 最大允许的数据年龄
     * @return 市场深度快照
     */
    Optional<MarketDepthSnapshot> getDepth(String symbol, Duration maxAge);
    
    /**
     * 检查是否支持指定的交易对
     * @param symbol 交易对符号
     * @return 如果支持返回true
     */
    boolean isSymbolSupported(String symbol);
    
    /**
     * 获取所有支持的交易对列表
     * @return 支持的交易对符号列表
     */
    List<String> getSupportedSymbols();
    
    /**
     * 获取指定交易对的最后更新时间
     * @param symbol 交易对符号
     * @return 最后更新时间的毫秒时间戳，如果不可用返回-1
     */
    long getLastUpdateTime(String symbol);
    
    /**
     * 检查指定交易对的数据是否可用且新鲜
     * @param symbol 交易对符号
     * @param maxAge 最大允许的数据年龄
     * @return 如果数据可用且新鲜返回true
     */
    default boolean isDataFresh(String symbol, Duration maxAge) {
        Optional<MarketDepthSnapshot> snapshot = getDepth(symbol, maxAge);
        return snapshot.isPresent() && snapshot.get().hasValidDepth();
    }
}
