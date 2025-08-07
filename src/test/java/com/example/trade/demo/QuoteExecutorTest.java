package com.example.trade.demo;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.example.trade.demo.domain.entity.QuoteInstruction;
import com.example.trade.demo.domain.service.QuoteExecutor;

public class QuoteExecutorTest {
    
    private QuoteExecutor executor;
    
    @BeforeEach
    void setUp() {
        executor = new QuoteExecutor();
    }
    
    @Test
    void testValidQuoteExecution() {
        // 测试有效的报价：买价=80，卖价=90
        QuoteInstruction instruction = new QuoteInstruction("BTCUSDT", 
            new BigDecimal("80.0"), new BigDecimal("90.0"));
        
        var result = executor.executeQuote(instruction);
        
        assertTrue(result.isSuccess(), "有效的报价应该执行成功");
        assertEquals("执行成功", result.getMessage());
    }
    
    @Test
    void testBidPriceTooHigh() {
        // 测试买价过高：买价=120（超过上限100）
        QuoteInstruction instruction = new QuoteInstruction("BTCUSDT", 
            new BigDecimal("120.0"), new BigDecimal("130.0"));
        
        var result = executor.executeQuote(instruction);
        
        assertFalse(result.isSuccess(), "买价过高应该执行失败");
        assertEquals("风控检查失败", result.getMessage());
    }
    
    @Test
    void testAskPriceTooLow() {
        // 测试卖价过低：卖价=30（低于下限50）
        QuoteInstruction instruction = new QuoteInstruction("BTCUSDT", 
            new BigDecimal("20.0"), new BigDecimal("30.0"));
        
        var result = executor.executeQuote(instruction);
        
        assertFalse(result.isSuccess(), "卖价过低应该执行失败");
        assertEquals("风控检查失败", result.getMessage());
    }
    
    @Test
    void testBidHigherThanAsk() {
        // 测试买价高于卖价
        QuoteInstruction instruction = new QuoteInstruction("BTCUSDT", 
            new BigDecimal("90.0"), new BigDecimal("80.0"));
        
        var result = executor.executeQuote(instruction);
        
        assertFalse(result.isSuccess(), "买价高于卖价应该执行失败");
        assertEquals("风控检查失败", result.getMessage());
    }
    
    @Test
    void testZeroPrice() {
        // 测试零价格
        QuoteInstruction instruction = new QuoteInstruction("BTCUSDT", 
            BigDecimal.ZERO, new BigDecimal("90.0"));
        
        var result = executor.executeQuote(instruction);
        
        assertFalse(result.isSuccess(), "零价格应该执行失败");
        assertEquals("风控检查失败", result.getMessage());
    }
    
    @Test
    void testNegativePrice() {
        // 测试负价格
        QuoteInstruction instruction = new QuoteInstruction("BTCUSDT", 
            new BigDecimal("-10.0"), new BigDecimal("90.0"));
        
        var result = executor.executeQuote(instruction);
        
        assertFalse(result.isSuccess(), "负价格应该执行失败");
        assertEquals("风控检查失败", result.getMessage());
    }
} 