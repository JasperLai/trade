package com.example.trade.demo.domain.valueobject;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 报价请求值对象
 * 支持RFQ(询价报价)和ESP(连续报价)两种模式
 */
public class QuoteRequest {
    
    public enum Mode {
        RFQ,  // Request for Quote - 询价报价
        ESP   // Electronic Streaming Pricing - 连续报价
    }
    
    private final String symbol;
    private final Mode mode;
    private final Optional<BigDecimal> requestQuantity; // RFQ时指定数量
    private final Optional<Duration> validityPeriod;   // 有效期
    private final LocalDateTime requestTime;
    private final String requestId;
    
    public QuoteRequest(String symbol, Mode mode, Optional<BigDecimal> requestQuantity, 
                       Optional<Duration> validityPeriod) {
        this.symbol = symbol;
        this.mode = mode;
        this.requestQuantity = requestQuantity;
        this.validityPeriod = validityPeriod;
        this.requestTime = LocalDateTime.now();
        this.requestId = "QR-" + System.currentTimeMillis() + "-" + symbol;
    }
    
    // 便利构造方法
    public static QuoteRequest createRfq(String symbol, BigDecimal quantity, Duration validityPeriod) {
        return new QuoteRequest(symbol, Mode.RFQ, Optional.of(quantity), Optional.of(validityPeriod));
    }
    
    public static QuoteRequest createEsp(String symbol) {
        return new QuoteRequest(symbol, Mode.ESP, Optional.empty(), Optional.empty());
    }
    
    // 业务方法
    public boolean isValid() {
        if (symbol == null || symbol.trim().isEmpty()) {
            return false;
        }
        if (mode == Mode.RFQ && !requestQuantity.isPresent()) {
            return false;
        }
        return true;
    }
    
    public boolean isExpired() {
        if (validityPeriod.isPresent()) {
            return requestTime.plus(validityPeriod.get()).isBefore(LocalDateTime.now());
        }
        return false; // ESP模式无过期概念
    }
    
    public boolean requiresQuantity() {
        return mode == Mode.RFQ;
    }
    
    // Getter方法
    public String getSymbol() { return symbol; }
    public Mode getMode() { return mode; }
    public Optional<BigDecimal> getRequestQuantity() { return requestQuantity; }
    public Optional<Duration> getValidityPeriod() { return validityPeriod; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public String getRequestId() { return requestId; }
    
    @Override
    public String toString() {
        return String.format("QuoteRequest{id='%s', symbol='%s', mode=%s, quantity=%s, requestId='%s'}",
                           requestId, symbol, mode, 
                           requestQuantity.map(BigDecimal::toString).orElse("N/A"),
                           requestId);
    }
}
