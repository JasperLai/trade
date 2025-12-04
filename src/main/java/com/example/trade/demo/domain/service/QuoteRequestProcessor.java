package com.example.trade.demo.domain.service;

import java.time.Duration;
import java.util.Optional;

import com.example.trade.demo.domain.entity.ExecutionResult;
import com.example.trade.demo.domain.entity.QuoteInstruction;
import com.example.trade.demo.domain.valueobject.MarketDepthSnapshot;
import com.example.trade.demo.domain.valueobject.QuoteRequest;

/**
 * 报价请求处理器
 * 处理RFQ和ESP模式的报价请求
 */
public class QuoteRequestProcessor {
    
    private final QuoteStrategy strategy;
    private final QuoteExecutor executor;
    private final MarketDepthProvider marketDepthProvider;
    
    public QuoteRequestProcessor(QuoteStrategy strategy, 
                               QuoteExecutor executor,
                               MarketDepthProvider marketDepthProvider) {
        this.strategy = strategy;
        this.executor = executor;
        this.marketDepthProvider = marketDepthProvider;
    }
    
    /**
     * 处理报价请求
     * @param request 报价请求
     * @return 处理结果
     */
    public QuoteProcessingResult processRequest(QuoteRequest request) {
        System.out.println("[QuoteRequestProcessor] 处理报价请求: " + request);
        
        // 1. 验证请求有效性
        if (!request.isValid()) {
            System.out.println("[QuoteRequestProcessor] 无效的报价请求: " + request);
            return QuoteProcessingResult.invalidRequest("报价请求无效");
        }
        
        // 2. 检查请求是否过期
        if (request.isExpired()) {
            System.out.println("[QuoteRequestProcessor] 报价请求已过期: " + request);
            return QuoteProcessingResult.expiredRequest("报价请求已过期");
        }
        
        // 3. 检查策略是否能处理
        if (!strategy.canHandle(request)) {
            System.out.println("[QuoteRequestProcessor] 策略无法处理该请求: " + request);
            return QuoteProcessingResult.unsupportedRequest("策略不支持该类型的报价请求");
        }
        
        // 4. 获取当前市场深度快照
        Optional<MarketDepthSnapshot> snapshotOpt = getMarketDepth(request.getSymbol(), Duration.ofSeconds(1));
        if (snapshotOpt.isEmpty()) {
            System.out.println("[QuoteRequestProcessor] 无法获取市场深度数据: " + request.getSymbol());
            return QuoteProcessingResult.noMarketData("无法获取市场深度数据");
        }
        
        MarketDepthSnapshot snapshot = snapshotOpt.get();
        
        // 5. 检查数据有效性
        if (!snapshot.hasValidDepth()) {
            System.out.println("[QuoteRequestProcessor] 市场深度数据无效: " + request.getSymbol());
            return QuoteProcessingResult.invalidMarketData("市场深度数据无效");
        }
        
        // 6. 调用策略生成报价
        QuoteInstruction instruction = strategy.decideQuote(snapshot, request);
        if (instruction == null) {
            System.out.println("[QuoteRequestProcessor] 策略未生成报价指令");
            return QuoteProcessingResult.noQuoteGenerated("策略未生成报价指令");
        }
        
        // 7. 执行风控和报价
        ExecutionResult executionResult = executor.executeQuote(instruction);
        
        // 8. 评估报价质量
        int quality = strategy.assessQuoteQuality(instruction, snapshot);
        
        System.out.println("[QuoteRequestProcessor] 报价处理完成，质量评分: " + quality);
        
        return QuoteProcessingResult.success(instruction, executionResult, quality, snapshot);
    }
    
    /**
     * 获取市场深度数据
     * @param symbol 交易对符号
     * @param maxAge 最大数据年龄
     * @return 市场深度快照
     */
    private Optional<MarketDepthSnapshot> getMarketDepth(String symbol, Duration maxAge) {
        if (!marketDepthProvider.isSymbolSupported(symbol)) {
            System.out.println("[QuoteRequestProcessor] 不支持的交易对: " + symbol);
            return Optional.empty();
        }
        
        return marketDepthProvider.getDepth(symbol, maxAge);
    }
    
    /**
     * 获取支持的交易对列表
     * @return 支持的交易对列表
     */
    public java.util.List<String> getSupportedSymbols() {
        return marketDepthProvider.getSupportedSymbols();
    }
    
    /**
     * 检查数据是否新鲜
     * @param symbol 交易对符号
     * @return 如果数据新鲜返回true
     */
    public boolean isDataFresh(String symbol) {
        return marketDepthProvider.isDataFresh(symbol, Duration.ofSeconds(1));
    }
    
    /**
     * 报价处理结果值对象
     */
    public static class QuoteProcessingResult {
        
        public enum Status {
            SUCCESS,             // 成功
            INVALID_REQUEST,     // 无效请求
            EXPIRED_REQUEST,     // 请求过期
            UNSUPPORTED_REQUEST,  // 不支持的请求
            NO_MARKET_DATA,      // 无市场数据
            INVALID_MARKET_DATA,  // 无效市场数据
            NO_QUOTE_GENERATED   // 未生成报价
        }
        
        private final Status status;
        private final String message;
        private final QuoteInstruction instruction;
        private final ExecutionResult executionResult;
        private final Integer quality;
        private final MarketDepthSnapshot snapshot;
        
        private QuoteProcessingResult(Status status, String message, QuoteInstruction instruction, 
                                   ExecutionResult executionResult, Integer quality, MarketDepthSnapshot snapshot) {
            this.status = status;
            this.message = message;
            this.instruction = instruction;
            this.executionResult = executionResult;
            this.quality = quality;
            this.snapshot = snapshot;
        }
        
        public static QuoteProcessingResult success(QuoteInstruction instruction, ExecutionResult executionResult, 
                                                  int quality, MarketDepthSnapshot snapshot) {
            return new QuoteProcessingResult(Status.SUCCESS, "报价处理成功", 
                                        instruction, executionResult, quality, snapshot);
        }
        
        public static QuoteProcessingResult invalidRequest(String message) {
            return new QuoteProcessingResult(Status.INVALID_REQUEST, message, null, null, null, null);
        }
        
        public static QuoteProcessingResult expiredRequest(String message) {
            return new QuoteProcessingResult(Status.EXPIRED_REQUEST, message, null, null, null, null);
        }
        
        public static QuoteProcessingResult unsupportedRequest(String message) {
            return new QuoteProcessingResult(Status.UNSUPPORTED_REQUEST, message, null, null, null, null);
        }
        
        public static QuoteProcessingResult noMarketData(String message) {
            return new QuoteProcessingResult(Status.NO_MARKET_DATA, message, null, null, null, null);
        }
        
        public static QuoteProcessingResult invalidMarketData(String message) {
            return new QuoteProcessingResult(Status.INVALID_MARKET_DATA, message, null, null, null, null);
        }
        
        public static QuoteProcessingResult noQuoteGenerated(String message) {
            return new QuoteProcessingResult(Status.NO_QUOTE_GENERATED, message, null, null, null, null);
        }
        
        // Getter方法
        public Status getStatus() { return status; }
        public String getMessage() { return message; }
        public QuoteInstruction getInstruction() { return instruction; }
        public ExecutionResult getExecutionResult() { return executionResult; }
        public Integer getQuality() { return quality; }
        public MarketDepthSnapshot getSnapshot() { return snapshot; }
        
        public boolean isSuccess() { return status == Status.SUCCESS; }
        
        @Override
        public String toString() {
            return String.format("QuoteProcessingResult{status=%s, message='%s', quality=%s}",
                               status, message, quality != null ? quality : "N/A");
        }
    }
}
