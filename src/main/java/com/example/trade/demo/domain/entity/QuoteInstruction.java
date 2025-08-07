package com.example.trade.demo.domain.entity;

import java.math.BigDecimal;

// 报价指令 - 支持三种报价模式
public class QuoteInstruction {
    public enum QuoteType {
        BID,        // 买价模式
        ASK,        // 卖价模式  
        BID_ASK     // 买卖价模式
    }
    
    private final String symbol;
    private final QuoteType quoteType;
    private final BigDecimal bidPrice; // 买价
    private final BigDecimal askPrice; // 卖价
    private final BigDecimal size;
    private final String strategyName;
    private final long timestamp;

    // 买价模式构造函数
    public QuoteInstruction(String symbol, BigDecimal bidPrice, BigDecimal size, String strategyName) {
        this.symbol = symbol;
        this.quoteType = QuoteType.BID;
        this.bidPrice = bidPrice;
        this.askPrice = null;
        this.size = size;
        this.strategyName = strategyName;
        this.timestamp = System.currentTimeMillis();
    }
    
    // 卖价模式构造函数
    public QuoteInstruction(String symbol, QuoteType quoteType, BigDecimal askPrice, BigDecimal size, String strategyName) {
        this.symbol = symbol;
        this.quoteType = QuoteType.ASK;
        this.bidPrice = null;
        this.askPrice = askPrice;
        this.size = size;
        this.strategyName = strategyName;
        this.timestamp = System.currentTimeMillis();
    }
    
    // 买卖价模式构造函数
    public QuoteInstruction(String symbol, BigDecimal bidPrice, BigDecimal askPrice, BigDecimal size, String strategyName) {
        this.symbol = symbol;
        this.quoteType = QuoteType.BID_ASK;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.size = size;
        this.strategyName = strategyName;
        this.timestamp = System.currentTimeMillis();
    }
    
    // 兼容性构造函数（保持向后兼容）
    public QuoteInstruction(String symbol, BigDecimal bidPrice, BigDecimal askPrice) {
        this.symbol = symbol;
        this.quoteType = QuoteType.BID_ASK;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.size = BigDecimal.ONE;
        this.strategyName = "SimpleBestPriceStrategy";
        this.timestamp = System.currentTimeMillis();
    }

    // 静态工厂方法 - 创建买价指令
    public static QuoteInstruction createBidQuote(String symbol, BigDecimal bidPrice, BigDecimal size, String strategyName) {
        return new QuoteInstruction(symbol, bidPrice, size, strategyName);
    }
    
    // 静态工厂方法 - 创建卖价指令
    public static QuoteInstruction createAskQuote(String symbol, BigDecimal askPrice, BigDecimal size, String strategyName) {
        return new QuoteInstruction(symbol, QuoteType.ASK, askPrice, size, strategyName);
    }
    
    // 静态工厂方法 - 创建买卖价指令
    public static QuoteInstruction createBidAskQuote(String symbol, BigDecimal bidPrice, BigDecimal askPrice, BigDecimal size, String strategyName) {
        return new QuoteInstruction(symbol, bidPrice, askPrice, size, strategyName);
    }

    // Getter方法
    public String getSymbol() { return symbol; }
    public QuoteType getQuoteType() { return quoteType; }
    public BigDecimal getBidPrice() { return bidPrice; }
    public BigDecimal getAskPrice() { return askPrice; }
    public BigDecimal getSize() { return size; }
    public String getStrategyName() { return strategyName; }
    public long getTimestamp() { return timestamp; }
    
    // 兼容性方法
    public String getSide() { 
        switch (quoteType) {
            case BID: return "BID";
            case ASK: return "ASK";
            case BID_ASK: return "BID/ASK";
            default: return "UNKNOWN";
        }
    }
    
    public BigDecimal getPrice() { 
        return quoteType == QuoteType.BID ? bidPrice : askPrice; 
    }

    // 业务方法
    public boolean isBidQuote() {
        return quoteType == QuoteType.BID;
    }
    
    public boolean isAskQuote() {
        return quoteType == QuoteType.ASK;
    }
    
    public boolean isBidAskQuote() {
        return quoteType == QuoteType.BID_ASK;
    }
    
    public BigDecimal getQuotePrice() {
        switch (quoteType) {
            case BID: return bidPrice;
            case ASK: return askPrice;
            case BID_ASK: return bidPrice; // 对于买卖价模式，返回买价作为主要价格
            default: return null;
        }
    }

    @Override
    public String toString() {
        return String.format("QuoteInstruction{symbol='%s', type=%s, bid=%s, ask=%s, size=%s, strategy='%s'}",
                           symbol, quoteType, bidPrice, askPrice, size, strategyName);
    }
}
