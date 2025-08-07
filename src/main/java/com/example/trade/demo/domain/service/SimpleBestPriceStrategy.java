package com.example.trade.demo.domain.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.example.trade.demo.domain.entity.MarketDepthAggregator;
import com.example.trade.demo.domain.entity.QuoteInstruction;

public class SimpleBestPriceStrategy implements QuoteStrategy{
    
    // 策略类型枚举
    public enum StrategyType {
        BID_ONLY,      // 只报买价
        ASK_ONLY,      // 只报卖价
        BID_ASK        // 报买卖价
    }
    
    private final StrategyType strategyType;
    
    public SimpleBestPriceStrategy() {
        this.strategyType = StrategyType.BID_ASK; // 默认使用买卖价模式
    }
    
    public SimpleBestPriceStrategy(StrategyType strategyType) {
        this.strategyType = strategyType;
    }
    
    @Override
    public QuoteInstruction decideQuote(MarketDepthAggregator aggregator) {
        System.out.println("[Strategy] 策略决定报价: " + aggregator.getSymbol());
        Optional<BigDecimal> bestBid = aggregator.getBestBid();
        Optional<BigDecimal> bestAsk = aggregator.getBestAsk();

        if (!bestBid.isPresent() || !bestAsk.isPresent()) {
            System.out.println("[Strategy] 无法获取最优价格，不生成指令");
            return null;
        }

        BigDecimal bidPrice = bestBid.get();
        BigDecimal askPrice = bestAsk.get();
        
        // 根据策略类型生成不同的报价指令
        switch (strategyType) {
            case BID_ONLY:
                return createBidOnlyQuote(aggregator.getSymbol(), bidPrice);
            case ASK_ONLY:
                return createAskOnlyQuote(aggregator.getSymbol(), askPrice);
            case BID_ASK:
            default:
                return createBidAskQuote(aggregator.getSymbol(), bidPrice, askPrice);
        }
    }
    
    private QuoteInstruction createBidOnlyQuote(String symbol, BigDecimal bidPrice) {
        QuoteInstruction instruction = QuoteInstruction.createBidQuote(
            symbol, bidPrice, BigDecimal.ONE, "SimpleBestPriceStrategy-BID"
        );
        System.out.println("[Strategy] 生成买价指令: " + instruction);
        return instruction;
    }
    
    private QuoteInstruction createAskOnlyQuote(String symbol, BigDecimal askPrice) {
        QuoteInstruction instruction = QuoteInstruction.createAskQuote(
            symbol, askPrice, BigDecimal.ONE, "SimpleBestPriceStrategy-ASK"
        );
        System.out.println("[Strategy] 生成卖价指令: " + instruction);
        return instruction;
    }
    
    private QuoteInstruction createBidAskQuote(String symbol, BigDecimal bidPrice, BigDecimal askPrice) {
        QuoteInstruction instruction = QuoteInstruction.createBidAskQuote(
            symbol, bidPrice, askPrice, BigDecimal.ONE, "SimpleBestPriceStrategy-BID_ASK"
        );
        System.out.println("[Strategy] 生成买卖价指令: " + instruction);
        return instruction;
    }
}

