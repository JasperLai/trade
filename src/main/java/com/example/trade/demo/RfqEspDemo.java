package com.example.trade.demo;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.trade.demo.domain.entity.MarketDataEvent;
import com.example.trade.demo.domain.entity.OrderBookLevel;
import com.example.trade.demo.domain.service.InMemoryMarketDepthProvider;
import com.example.trade.demo.domain.service.QuoteExecutor;
import com.example.trade.demo.domain.service.QuoteRequestProcessor;
import com.example.trade.demo.domain.service.SimpleBestPriceStrategy;
import com.example.trade.demo.domain.service.VMAPBestPriceStrategy;
import com.example.trade.demo.domain.valueobject.QuoteRequest;

/**
 * RFQ和ESP模式演示程序
 * 展示新的架构设计
 */
@SpringBootApplication
public class RfqEspDemo {

    public static void main(String[] args) {
        SpringApplication.run(RfqEspDemo.class, args);
    }

    @Bean
    public SimpleBestPriceStrategy simpleBestPriceStrategy() {
        return new SimpleBestPriceStrategy(SimpleBestPriceStrategy.StrategyType.BID_ASK);
    }

    @Bean
    public VMAPBestPriceStrategy vmapBestPriceStrategy() {
        return new VMAPBestPriceStrategy(
                new VMAPBestPriceStrategy.Params(
                        new BigDecimal("50"), // targetQty：希望覆盖 50 手的深度
                        5, // maxLevels：最多看 5 档
                        new BigDecimal("30"), // minDepth：少于 30 手不报
                        new BigDecimal("0.01"), // tickSize
                        +1, // bidSteps：在 VWAP 基础上加 1 tick
                        +1, // askSteps：在 VWAP 基础上加 1 tick（更保守）
                        new BigDecimal("5"), // quoteSize：每边挂 5 手
                        "VWAP_BEST"));
    }

    @Bean
    public CommandLineRunner rfqEspDemoRunner(
            @Autowired InMemoryMarketDepthProvider marketDepthProvider,
            @Autowired QuoteExecutor executor) {
        
        // 创建策略处理器
        SimpleBestPriceStrategy simpleStrategy = new SimpleBestPriceStrategy(SimpleBestPriceStrategy.StrategyType.BID_ASK);
        VMAPBestPriceStrategy vmapStrategy = new VMAPBestPriceStrategy(
                new VMAPBestPriceStrategy.Params(
                        new BigDecimal("50"), // targetQty：希望覆盖 50 手的深度
                        5, // maxLevels：最多看 5 档
                        new BigDecimal("30"), // minDepth：少于 30 手不报
                        new BigDecimal("0.01"), // tickSize
                        +1, // bidSteps：在 VWAP 基础上加 1 tick
                        +1, // askSteps：在 VWAP 基础上加 1 tick（更保守）
                        new BigDecimal("5"), // quoteSize：每边挂 5 手
                        "VWAP_BEST"));
        
        // 创建处理器
        QuoteRequestProcessor simpleProcessor = new QuoteRequestProcessor(
                simpleStrategy, executor, marketDepthProvider);
        QuoteRequestProcessor vmapProcessor = new QuoteRequestProcessor(
                vmapStrategy, executor, marketDepthProvider);
        
        return args -> {
            System.out.println("=== RFQ和ESP模式演示开始 ===\n");

            // 初始化一些市场数据
            initializeMarketData(marketDepthProvider);

            // 演示1：RFQ模式 - 简单策略
            System.out.println("【演示1】RFQ模式 - 简单最优价格策略");
            demoRfqMode(simpleProcessor, "BTCUSDT", new BigDecimal("10"));

            System.out.println("\n" + "=".repeat(50) + "\n");

            // 演示2：RFQ模式 - VWAP策略
            System.out.println("【演示2】RFQ模式 - VWAP策略");
            demoRfqMode(vmapProcessor, "BTCUSDT", new BigDecimal("100"));

            System.out.println("\n" + "=".repeat(50) + "\n");

            // 演示3：ESP模式 - 简单策略
            System.out.println("【演示3】ESP模式 - 简单最优价格策略");
            demoEspMode(simpleProcessor, "ETHUSDT");

            System.out.println("\n" + "=".repeat(50) + "\n");

            // 演示4：ESP模式 - VWAP策略
            System.out.println("【演示4】ESP模式 - VWAP策略");
            demoEspMode(vmapProcessor, "ETHUSDT");

            System.out.println("\n=== RFQ和ESP模式演示结束 ===");
        };
    }

    /**
     * 演示RFQ模式
     */
    private static void demoRfqMode(QuoteRequestProcessor processor, String symbol, BigDecimal quantity) {
        QuoteRequest rfqRequest = QuoteRequest.createRfq(symbol, quantity, Duration.ofSeconds(5));
        
        System.out.println("发送RFQ请求: " + rfqRequest);
        QuoteRequestProcessor.QuoteProcessingResult result = processor.processRequest(rfqRequest);
        
        System.out.println("RFQ处理结果: " + result);
        if (result.isSuccess()) {
            System.out.println("生成报价: " + result.getInstruction());
            System.out.println("执行结果: " + result.getExecutionResult());
            System.out.println("报价质量评分: " + result.getQuality());
        } else {
            System.out.println("处理失败: " + result.getMessage());
        }
    }

    /**
     * 演示ESP模式
     */
    private static void demoEspMode(QuoteRequestProcessor processor, String symbol) {
        QuoteRequest espRequest = QuoteRequest.createEsp(symbol);
        
        System.out.println("发送ESP请求: " + espRequest);
        QuoteRequestProcessor.QuoteProcessingResult result = processor.processRequest(espRequest);
        
        System.out.println("ESP处理结果: " + result);
        if (result.isSuccess()) {
            System.out.println("生成报价: " + result.getInstruction());
            System.out.println("执行结果: " + result.getExecutionResult());
            System.out.println("报价质量评分: " + result.getQuality());
            System.out.println("市场快照: " + result.getSnapshot());
        } else {
            System.out.println("处理失败: " + result.getMessage());
        }
    }

    /**
     * 初始化市场数据
     */
    private static void initializeMarketData(InMemoryMarketDepthProvider provider) {
        // 更新BTCUSDT数据
        var btcAggregator = provider.getAggregator("BTCUSDT");
        if (btcAggregator != null) {
            MarketDataEvent btcEvent = new MarketDataEvent(
                "BTCUSDT", "ProviderA",
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("100.5"), new BigDecimal("10")),
                    new OrderBookLevel(new BigDecimal("100.6"), new BigDecimal("20")),
                    new OrderBookLevel(new BigDecimal("100.7"), new BigDecimal("15"))
                ),
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("100.4"), new BigDecimal("15")),
                    new OrderBookLevel(new BigDecimal("100.3"), new BigDecimal("25")),
                    new OrderBookLevel(new BigDecimal("100.2"), new BigDecimal("10"))
                )
            );
            btcAggregator.updateDepth("ProviderA", btcEvent.getAskLevels(), btcEvent.getBidLevels());
            
            // 添加第二个提供商的数据
            MarketDataEvent btcEvent2 = new MarketDataEvent(
                "BTCUSDT", "ProviderB",
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("100.55"), new BigDecimal("8")),
                    new OrderBookLevel(new BigDecimal("100.65"), new BigDecimal("12"))
                ),
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("100.35"), new BigDecimal("18")),
                    new OrderBookLevel(new BigDecimal("100.25"), new BigDecimal("7"))
                )
            );
            btcAggregator.updateDepth("ProviderB", btcEvent2.getAskLevels(), btcEvent2.getBidLevels());
        }

        // 更新ETHUSDT数据
        var ethAggregator = provider.getAggregator("ETHUSDT");
        if (ethAggregator != null) {
            MarketDataEvent ethEvent = new MarketDataEvent(
                "ETHUSDT", "ProviderC",
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("50.1"), new BigDecimal("5")),
                    new OrderBookLevel(new BigDecimal("50.2"), new BigDecimal("8")),
                    new OrderBookLevel(new BigDecimal("50.3"), new BigDecimal("12"))
                ),
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("50.0"), new BigDecimal("7")),
                    new OrderBookLevel(new BigDecimal("49.9"), new BigDecimal("15")),
                    new OrderBookLevel(new BigDecimal("49.8"), new BigDecimal("9"))
                )
            );
            ethAggregator.updateDepth("ProviderC", ethEvent.getAskLevels(), ethEvent.getBidLevels());
            
            // 添加第二个提供商的数据
            MarketDataEvent ethEvent2 = new MarketDataEvent(
                "ETHUSDT", "ProviderD",
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("50.15"), new BigDecimal("6")),
                    new OrderBookLevel(new BigDecimal("50.25"), new BigDecimal("10"))
                ),
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("49.95"), new BigDecimal("14")),
                    new OrderBookLevel(new BigDecimal("49.85"), new BigDecimal("8"))
                )
            );
            ethAggregator.updateDepth("ProviderD", ethEvent2.getAskLevels(), ethEvent2.getBidLevels());
        }

        System.out.println("市场数据初始化完成");
        System.out.println("支持的交易对: " + provider.getSupportedSymbols());
    }
}
