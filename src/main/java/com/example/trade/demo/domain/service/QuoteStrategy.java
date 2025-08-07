package com.example.trade.demo.domain.service;

import com.example.trade.demo.domain.entity.MarketDepthAggregator;
import com.example.trade.demo.domain.entity.QuoteInstruction;

// 2. QuoteStrategy (接口和实现)
public interface QuoteStrategy {
    QuoteInstruction decideQuote(MarketDepthAggregator aggregator);
    // 简化，去掉 MarketContext
}
