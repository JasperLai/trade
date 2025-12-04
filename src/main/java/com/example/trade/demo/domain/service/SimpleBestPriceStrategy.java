package com.example.trade.demo.domain.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.example.trade.demo.domain.entity.QuoteInstruction;
import com.example.trade.demo.domain.valueobject.MarketDepthSnapshot;
import com.example.trade.demo.domain.valueobject.QuoteRequest;

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
    public QuoteInstruction decideQuote(MarketDepthSnapshot snapshot, QuoteRequest request) {
        System.out.println("[SimpleBestPriceStrategy] 策略决定报价: " + snapshot.getSymbol());
        Optional<BigDecimal> bestBid = snapshot.getBestBid();
        Optional<BigDecimal> bestAsk = snapshot.getBestAsk();

        if (!bestBid.isPresent() || !bestAsk.isPresent()) {
            System.out.println("[SimpleBestPriceStrategy] 无法获取最优价格，不生成指令");
            return null;
        }

        BigDecimal bidPrice = bestBid.get();
        BigDecimal askPrice = bestAsk.get();
        String symbol = snapshot.getSymbol();
        
        // 根据策略类型和请求模式生成不同的报价指令
        switch (strategyType) {
            case BID_ONLY:
                return createBidOnlyQuote(symbol, bidPrice, request);
            case ASK_ONLY:
                return createAskOnlyQuote(symbol, askPrice, request);
            case BID_ASK:
            default:
                return createBidAskQuote(symbol, bidPrice, askPrice, request);
        }
    }
    
    @Override
    public boolean canHandle(QuoteRequest request) {
        // 简单最优价格策略可以处理所有类型的请求
        return request.isValid();
    }
    
    @Override
    public String getStrategyName() {
        return "SimpleBestPriceStrategy-" + strategyType;
    }
    
    private QuoteInstruction createBidOnlyQuote(String symbol, BigDecimal bidPrice, QuoteRequest request) {
        BigDecimal size = request.getRequestQuantity().orElse(BigDecimal.ONE);
        QuoteInstruction instruction = QuoteInstruction.createBidQuote(
            symbol, bidPrice, size, getStrategyName()
        );
        System.out.println("[SimpleBestPriceStrategy] 生成买价指令: " + instruction);
        return instruction;
    }
    
    private QuoteInstruction createAskOnlyQuote(String symbol, BigDecimal askPrice, QuoteRequest request) {
        BigDecimal size = request.getRequestQuantity().orElse(BigDecimal.ONE);
        QuoteInstruction instruction = QuoteInstruction.createAskQuote(
            symbol, askPrice, size, getStrategyName()
        );
        System.out.println("[SimpleBestPriceStrategy] 生成卖价指令: " + instruction);
        return instruction;
    }
    
    private QuoteInstruction createBidAskQuote(String symbol, BigDecimal bidPrice, BigDecimal askPrice, QuoteRequest request) {
        BigDecimal size = request.getRequestQuantity().orElse(BigDecimal.ONE);
        QuoteInstruction instruction = QuoteInstruction.createBidAskQuote(
            symbol, bidPrice, askPrice, size, getStrategyName()
        );
        System.out.println("[SimpleBestPriceStrategy] 生成买卖价指令: " + instruction);
        return instruction;
    }
}
