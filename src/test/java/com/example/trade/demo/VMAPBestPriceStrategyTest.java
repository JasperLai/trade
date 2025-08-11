package com.example.trade.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.example.trade.demo.domain.entity.MarketDepthAggregator;
import com.example.trade.demo.domain.entity.OrderBookLevel;
import com.example.trade.demo.domain.entity.QuoteInstruction;
import com.example.trade.demo.domain.service.VMAPBestPriceStrategy;

public class VMAPBestPriceStrategyTest {

    @Test
    void testVWAPQuoteHappyPath() {
        MarketDepthAggregator agg = new MarketDepthAggregator("BTCUSDT");

        // ProviderA 深度
        agg.updateDepth(
            "ProviderA",
            // asks
            Arrays.asList(
                new OrderBookLevel(new BigDecimal("100.50"), new BigDecimal("10")),
                new OrderBookLevel(new BigDecimal("100.60"), new BigDecimal("10"))
            ),
            // bids
            Arrays.asList(
                new OrderBookLevel(new BigDecimal("100.40"), new BigDecimal("10")),
                new OrderBookLevel(new BigDecimal("100.30"), new BigDecimal("10"))
            )
        );

        // ProviderB 深度
        agg.updateDepth(
            "ProviderB",
            // asks
            Arrays.asList(
                new OrderBookLevel(new BigDecimal("100.50"), new BigDecimal("5")),
                new OrderBookLevel(new BigDecimal("100.60"), new BigDecimal("15"))
            ),
            // bids
            Arrays.asList(
                new OrderBookLevel(new BigDecimal("100.30"), new BigDecimal("10"))
            )
        );

        VMAPBestPriceStrategy.Params params = new VMAPBestPriceStrategy.Params(
            new BigDecimal("20"),  // targetQty
            2,                      // maxLevels
            new BigDecimal("20"),  // minDepth
            new BigDecimal("0.01"),// tickSize
            0,                      // bidSteps
            0,                      // askSteps
            new BigDecimal("5"),   // quoteSize
            "VMAP_BEST_TEST"
        );

        VMAPBestPriceStrategy strat = new VMAPBestPriceStrategy(params);
        QuoteInstruction qi = strat.decideQuote(agg);

        assertNotNull(qi, "应生成买卖价指令");
        assertTrue(qi.isBidAskQuote(), "应为买卖价模式");
        assertEquals(new BigDecimal("100.35"), qi.getBidPrice());
        assertEquals(new BigDecimal("100.53"), qi.getAskPrice());
        assertEquals(new BigDecimal("5"), qi.getSize());
        assertEquals("VMAP_BEST_TEST", qi.getStrategyName());
    }

    @Test
    void testDepthTooShallowReturnsNull() {
        MarketDepthAggregator agg = new MarketDepthAggregator("ETHUSDT");

        agg.updateDepth(
            "ProviderA",
            Arrays.asList(
                new OrderBookLevel(new BigDecimal("50.10"), new BigDecimal("5"))
            ),
            Arrays.asList(
                new OrderBookLevel(new BigDecimal("49.90"), new BigDecimal("5"))
            )
        );

        VMAPBestPriceStrategy.Params params = new VMAPBestPriceStrategy.Params(
            new BigDecimal("10"),
            2,
            new BigDecimal("30"), // 需要的最小深度大于现有深度（仅 5）
            new BigDecimal("0.01"),
            0,
            0,
            new BigDecimal("1"),
            "VMAP_BEST_TEST"
        );

        VMAPBestPriceStrategy strat = new VMAPBestPriceStrategy(params);
        QuoteInstruction qi = strat.decideQuote(agg);
        assertNull(qi, "深度不足时不应生成指令");
    }
}


