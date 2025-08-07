package com.example.trade.demo;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.example.trade.demo.domain.entity.QuoteInstruction;

public class QuoteInstructionTest {
    
    @Test
    void testBidQuoteCreation() {
        // 测试买价模式
        QuoteInstruction bidQuote = QuoteInstruction.createBidQuote("BTCUSDT", new BigDecimal("100.0"), new BigDecimal("1.0"), "TestStrategy");
        
        assertTrue(bidQuote.isBidQuote());
        assertFalse(bidQuote.isAskQuote());
        assertFalse(bidQuote.isBidAskQuote());
        assertEquals(QuoteInstruction.QuoteType.BID, bidQuote.getQuoteType());
        assertEquals("BID", bidQuote.getSide());
        assertEquals(new BigDecimal("100.0"), bidQuote.getBidPrice());
        assertNull(bidQuote.getAskPrice());
        assertEquals(new BigDecimal("100.0"), bidQuote.getQuotePrice());
    }
    
    @Test
    void testAskQuoteCreation() {
        // 测试卖价模式
        QuoteInstruction askQuote = QuoteInstruction.createAskQuote("BTCUSDT", new BigDecimal("101.0"), new BigDecimal("1.0"), "TestStrategy");
        
        assertFalse(askQuote.isBidQuote());
        assertTrue(askQuote.isAskQuote());
        assertFalse(askQuote.isBidAskQuote());
        assertEquals(QuoteInstruction.QuoteType.ASK, askQuote.getQuoteType());
        assertEquals("ASK", askQuote.getSide());
        assertNull(askQuote.getBidPrice());
        assertEquals(new BigDecimal("101.0"), askQuote.getAskPrice());
        assertEquals(new BigDecimal("101.0"), askQuote.getQuotePrice());
    }
    
    @Test
    void testBidAskQuoteCreation() {
        // 测试买卖价模式
        QuoteInstruction bidAskQuote = QuoteInstruction.createBidAskQuote("BTCUSDT", new BigDecimal("100.0"), new BigDecimal("101.0"), new BigDecimal("1.0"), "TestStrategy");
        
        assertFalse(bidAskQuote.isBidQuote());
        assertFalse(bidAskQuote.isAskQuote());
        assertTrue(bidAskQuote.isBidAskQuote());
        assertEquals(QuoteInstruction.QuoteType.BID_ASK, bidAskQuote.getQuoteType());
        assertEquals("BID/ASK", bidAskQuote.getSide());
        assertEquals(new BigDecimal("100.0"), bidAskQuote.getBidPrice());
        assertEquals(new BigDecimal("101.0"), bidAskQuote.getAskPrice());
        assertEquals(new BigDecimal("100.0"), bidAskQuote.getQuotePrice()); // 买卖价模式返回买价作为主要价格
    }
    
    @Test
    void testCompatibilityConstructor() {
        // 测试兼容性构造函数
        QuoteInstruction compatibilityQuote = new QuoteInstruction("BTCUSDT", new BigDecimal("100.0"), new BigDecimal("101.0"));
        
        assertTrue(compatibilityQuote.isBidAskQuote());
        assertEquals(QuoteInstruction.QuoteType.BID_ASK, compatibilityQuote.getQuoteType());
        assertEquals("BID/ASK", compatibilityQuote.getSide());
        assertEquals(new BigDecimal("100.0"), compatibilityQuote.getBidPrice());
        assertEquals(new BigDecimal("101.0"), compatibilityQuote.getAskPrice());
    }
    
    @Test
    void testToString() {
        // 测试toString方法
        QuoteInstruction bidQuote = QuoteInstruction.createBidQuote("BTCUSDT", new BigDecimal("100.0"), new BigDecimal("1.0"), "TestStrategy");
        String toString = bidQuote.toString();
        
        assertTrue(toString.contains("BTCUSDT"));
        assertTrue(toString.contains("BID"));
        assertTrue(toString.contains("100.0"));
        assertTrue(toString.contains("TestStrategy"));
    }
} 