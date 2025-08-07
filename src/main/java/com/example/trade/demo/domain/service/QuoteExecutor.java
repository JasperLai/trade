package com.example.trade.demo.domain.service;

import java.math.BigDecimal;

import com.example.trade.demo.domain.entity.ExecutionResult;
import com.example.trade.demo.domain.entity.QuoteInstruction;

public class QuoteExecutor {
    // QuoteExecutor 的核心职责是执行指令并返回结果
    // 它不关心结果如何被记录或反馈，那是 Log 模块的事
    public ExecutionResult executeQuote(QuoteInstruction instruction) {
        System.out.println(String.format("[Executor] %s: 开始执行报价: %s", instruction.getSymbol(), instruction));

        // 1. 模拟风控检查
        if (!riskCheck(instruction)) {
            ExecutionResult result = new ExecutionResult(false, "风控检查失败", instruction);
            System.out.println(String.format("[Executor] %s: 风控失败: %s", instruction.getSymbol(), result));
            return result; // 直接返回失败结果
        }

        // 2. 模拟执行延迟和成功
        try {
            Thread.sleep(50); // 模拟执行时间
            System.out.println(String.format("[Executor] %s: 报价执行成功", instruction.getSymbol()));
            return new ExecutionResult(true, "执行成功", instruction); // 返回成功结果
        } catch (Exception e) {
            ExecutionResult result = new ExecutionResult(false, "执行异常: " + e.getMessage(), instruction);
            System.out.println(String.format("[Executor] %s: 执行异常: %s", instruction.getSymbol(), result));
            return result; // 返回异常结果
        }
    }

    private boolean riskCheck(QuoteInstruction instruction) {
        // 风控检查：根据不同的报价模式进行相应的检查
        QuoteInstruction.QuoteType quoteType = instruction.getQuoteType();
        
        switch (quoteType) {
            case BID:
                return checkBidQuote(instruction);
            case ASK:
                return checkAskQuote(instruction);
            case BID_ASK:
                return checkBidAskQuote(instruction);
            default:
                System.out.println("[Executor] 风控失败: 未知的报价类型");
                return false;
        }
    }
    
    private boolean checkBidQuote(QuoteInstruction instruction) {
        // 买价模式检查
        BigDecimal bidPrice = instruction.getBidPrice();
        
        // 基本检查：价格不能为空且必须为正数
        if (bidPrice == null) {
            System.out.println("[Executor] 风控失败: 买价为空");
            return false;
        }
        
        if (bidPrice.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("[Executor] 风控失败: 买价必须大于0");
            return false;
        }
        
        // 买价上限检查：买价不应当高于某个价格
        BigDecimal maxBidPrice = new BigDecimal("150.0"); // 买价上限
        if (bidPrice.compareTo(maxBidPrice) > 0) {
            System.out.println(String.format("[Executor] 风控失败: 买价 %.2f 高于上限 %.2f", bidPrice, maxBidPrice));
            return false;
        }
        
        System.out.println(String.format("[Executor] 买价风控检查通过: 买价=%.2f", bidPrice));
        return true;
    }
    
    private boolean checkAskQuote(QuoteInstruction instruction) {
        // 卖价模式检查
        BigDecimal askPrice = instruction.getAskPrice();
        
        // 基本检查：价格不能为空且必须为正数
        if (askPrice == null) {
            System.out.println("[Executor] 风控失败: 卖价为空");
            return false;
        }
        
        if (askPrice.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("[Executor] 风控失败: 卖价必须大于0");
            return false;
        }
        
        // 卖价下限检查：卖价不应当低于某个价格
        BigDecimal minAskPrice = new BigDecimal("30.0"); // 卖价下限
        if (askPrice.compareTo(minAskPrice) < 0) {
            System.out.println(String.format("[Executor] 风控失败: 卖价 %.2f 低于下限 %.2f", askPrice, minAskPrice));
            return false;
        }
        
        System.out.println(String.format("[Executor] 卖价风控检查通过: 卖价=%.2f", askPrice));
        return true;
    }
    
    private boolean checkBidAskQuote(QuoteInstruction instruction) {
        // 买卖价模式检查
        BigDecimal bidPrice = instruction.getBidPrice();
        BigDecimal askPrice = instruction.getAskPrice();
        
        // 基本检查：价格不能为空且必须为正数
        if (bidPrice == null || askPrice == null) {
            System.out.println("[Executor] 风控失败: 买价或卖价为空");
            return false;
        }
        
        if (bidPrice.compareTo(BigDecimal.ZERO) <= 0 || askPrice.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("[Executor] 风控失败: 价格必须大于0");
            return false;
        }
        
        // 买卖价差检查：买价必须小于卖价
        if (bidPrice.compareTo(askPrice) >= 0) {
            System.out.println("[Executor] 风控失败: 买价必须小于卖价");
            return false;
        }
        
        // 买价上限检查：买价不应当高于某个价格
        BigDecimal maxBidPrice = new BigDecimal("150.0"); // 买价上限
        if (bidPrice.compareTo(maxBidPrice) > 0) {
            System.out.println(String.format("[Executor] 风控失败: 买价 %.2f 高于上限 %.2f", bidPrice, maxBidPrice));
            return false;
        }
        
        // 卖价下限检查：卖价不应当低于某个价格
        BigDecimal minAskPrice = new BigDecimal("30.0"); // 卖价下限
        if (askPrice.compareTo(minAskPrice) < 0) {
            System.out.println(String.format("[Executor] 风控失败: 卖价 %.2f 低于下限 %.2f", askPrice, minAskPrice));
            return false;
        }
        
        System.out.println(String.format("[Executor] 买卖价风控检查通过: 买价=%.2f, 卖价=%.2f", bidPrice, askPrice));
        return true;
    }
}
