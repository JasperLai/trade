package com.example.trade.demo;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.trade.demo.domain.entity.MarketDataEvent;
import com.example.trade.demo.domain.entity.OrderBookLevel;
import com.example.trade.demo.domain.service.QuoteService;
import com.example.trade.demo.domain.service.SimpleBestPriceStrategy;

@SpringBootApplication
public class QuoteModeDemo {

    public static void main(String[] args) {
        SpringApplication.run(QuoteModeDemo.class, args);
    }

    @Bean
    public CommandLineRunner quoteModeDemoRunner() {
        return args -> {
            System.out.println("=== 三种报价模式演示开始 ===\n");

            // 准备测试数据
            MarketDataEvent testEvent = new MarketDataEvent(
                "BTCUSDT", "ProviderA",
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("100.5"), new BigDecimal("10")),
                    new OrderBookLevel(new BigDecimal("100.6"), new BigDecimal("20"))
                ),
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("100.4"), new BigDecimal("15")),
                    new OrderBookLevel(new BigDecimal("100.3"), new BigDecimal("25"))
                )
            );

            // 演示1：买价模式
            System.out.println("【演示1】买价模式 (BID_ONLY)");
            SimpleBestPriceStrategy bidStrategy = new SimpleBestPriceStrategy(SimpleBestPriceStrategy.StrategyType.BID_ONLY);
            QuoteService bidService = new QuoteService(bidStrategy);
            bidService.onMarketData(testEvent);

            System.out.println("\n" + "=".repeat(50) + "\n");

            // 演示2：卖价模式
            System.out.println("【演示2】卖价模式 (ASK_ONLY)");
            SimpleBestPriceStrategy askStrategy = new SimpleBestPriceStrategy(SimpleBestPriceStrategy.StrategyType.ASK_ONLY);
            QuoteService askService = new QuoteService(askStrategy);
            askService.onMarketData(testEvent);

            System.out.println("\n" + "=".repeat(50) + "\n");

            // 演示3：买卖价模式
            System.out.println("【演示3】买卖价模式 (BID_ASK)");
            SimpleBestPriceStrategy bidAskStrategy = new SimpleBestPriceStrategy(SimpleBestPriceStrategy.StrategyType.BID_ASK);
            QuoteService bidAskService = new QuoteService(bidAskStrategy);
            bidAskService.onMarketData(testEvent);

            System.out.println("\n" + "=".repeat(50) + "\n");

            // 演示4：风控测试 - 买价过高
            System.out.println("【演示4】风控测试 - 买价过高");
            MarketDataEvent highBidEvent = new MarketDataEvent(
                "BTCUSDT", "ProviderB",
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("200.0"), new BigDecimal("5"))
                ),
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("160.0"), new BigDecimal("8")) // 买价160超过上限150
                )
            );
            bidService.onMarketData(highBidEvent);

            System.out.println("\n" + "=".repeat(50) + "\n");

            // 演示5：风控测试 - 卖价过低
            System.out.println("【演示5】风控测试 - 卖价过低");
            MarketDataEvent lowAskEvent = new MarketDataEvent(
                "ETHUSDT", "ProviderC",
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("20.0"), new BigDecimal("5")) // 卖价20低于下限30
                ),
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("15.0"), new BigDecimal("8"))
                )
            );
            askService.onMarketData(lowAskEvent);

            System.out.println("\n=== 三种报价模式演示结束 ===");
        };
    }
} 