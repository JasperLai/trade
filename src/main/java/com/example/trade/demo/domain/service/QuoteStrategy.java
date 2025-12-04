package com.example.trade.demo.domain.service;

import com.example.trade.demo.domain.entity.QuoteInstruction;
import com.example.trade.demo.domain.valueobject.MarketDepthSnapshot;
import com.example.trade.demo.domain.valueobject.QuoteRequest;

/**
 * 报价策略接口
 * 根据市场深度快照和报价请求生成报价指令
 */
public interface QuoteStrategy {
    
    /**
     * 根据市场深度快照和报价请求决定报价
     * @param snapshot 市场深度快照
     * @param request 报价请求
     * @return 报价指令，如果不适合报价返回null
     */
    QuoteInstruction decideQuote(MarketDepthSnapshot snapshot, QuoteRequest request);
    
    /**
     * 检查策略是否能处理该报价请求
     * @param request 报价请求
     * @return 如果能处理返回true
     */
    boolean canHandle(QuoteRequest request);
    
    /**
     * 获取策略名称
     * @return 策略名称
     */
    String getStrategyName();
    
    /**
     * 评估报价质量（可选实现）
     * @param instruction 报价指令
     * @param snapshot 市场深度快照
     * @return 报价质量评分（0-100），越高越好
     */
    default int assessQuoteQuality(QuoteInstruction instruction, MarketDepthSnapshot snapshot) {
        // 默认实现：基于价差的质量评估
        return snapshot.getSpread()
                .map(spread -> {
                    // 价差越小，质量越高
                    if (spread.compareTo(new java.math.BigDecimal("0.01")) <= 0) return 100;
                    if (spread.compareTo(new java.math.BigDecimal("0.05")) <= 0) return 80;
                    if (spread.compareTo(new java.math.BigDecimal("0.1")) <= 0) return 60;
                    return 40;
                })
                .orElse(0);
    }
}
