package com.example.trade.demo;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.trade.demo.domain.entity.MarketDataEvent;
import com.example.trade.demo.domain.entity.OrderBookLevel;
import com.example.trade.demo.domain.service.QuoteService;
import com.example.trade.demo.domain.service.SimpleBestPriceStrategy;
import com.example.trade.demo.domain.service.VMAPBestPriceStrategy;

@SpringBootApplication
public class QuoteSystemDemo {

    public static void main(String[] args) {
        SpringApplication.run(QuoteSystemDemo.class, args);
    }

    @Bean
    public SimpleBestPriceStrategy simpleBestPriceStrategy() { return new SimpleBestPriceStrategy(); }

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
    public QuoteService quoteService(@Autowired VMAPBestPriceStrategy strategy) {
        return new QuoteService(strategy);
    }

    @Bean
    public CommandLineRunner demoRunner(@Autowired QuoteService service) {
        return args -> {
            System.out.println("=== 报价系统演示开始 ===\n");

            // 演示1：正常的行情数据
            System.out.println("【演示1】正常行情数据");
            MarketDataEvent event1 = new MarketDataEvent(
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
            service.onMarketData(event1);

            System.out.println("\n" + "=".repeat(50) + "\n");

            // 演示2：买价过高的行情数据
            System.out.println("【演示2】买价过高的行情数据（应该被风控拒绝）");
            MarketDataEvent event2 = new MarketDataEvent(
                "BTCUSDT", "ProviderB",
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("200.0"), new BigDecimal("5")), // 卖价200
                    new OrderBookLevel(new BigDecimal("201.0"), new BigDecimal("10"))
                ),
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("160.0"), new BigDecimal("8")), // 买价160（超过上限150）
                    new OrderBookLevel(new BigDecimal("159.0"), new BigDecimal("12"))
                )
            );
            service.onMarketData(event2);

            System.out.println("\n" + "=".repeat(50) + "\n");

            // 演示3：卖价过低的行情数据
            System.out.println("【演示3】卖价过低的行情数据（应该被风控拒绝）");
            MarketDataEvent event3 = new MarketDataEvent(
                "ETHUSDT", "ProviderC",
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("20.0"), new BigDecimal("5")), // 卖价20（低于下限30）
                    new OrderBookLevel(new BigDecimal("21.0"), new BigDecimal("10"))
                ),
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("15.0"), new BigDecimal("8")),
                    new OrderBookLevel(new BigDecimal("14.0"), new BigDecimal("12"))
                )
            );
            service.onMarketData(event3);

            System.out.println("\n" + "=".repeat(50) + "\n");

            // 演示4：另一个正常的行情数据
            System.out.println("【演示4】另一个正常行情数据");
            MarketDataEvent event4 = new MarketDataEvent(
                "ETHUSDT", "ProviderD",
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("80.0"), new BigDecimal("5")),
                    new OrderBookLevel(new BigDecimal("81.0"), new BigDecimal("10"))
                ),
                Arrays.asList(
                    new OrderBookLevel(new BigDecimal("75.0"), new BigDecimal("8")),
                    new OrderBookLevel(new BigDecimal("74.0"), new BigDecimal("12"))
                )
            );
            service.onMarketData(event4);

            System.out.println("\n=== 报价系统演示结束 ===");
        };
    }
} 