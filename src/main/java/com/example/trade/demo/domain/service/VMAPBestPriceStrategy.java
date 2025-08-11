package com.example.trade.demo.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import com.example.trade.demo.domain.entity.MarketDepthAggregator;
import com.example.trade.demo.domain.entity.QuoteInstruction;

/**
 * VMAP 报价策略（此处实现为 VWAP 口径，按聚合盘口计算目标量的加权均价），
 * 在该均价基础上加减若干 tick 形成买卖价，最终返回 BID_ASK 指令。
 */
public class VMAPBestPriceStrategy implements QuoteStrategy {

    public static final class Params {
        public final BigDecimal targetQty;
        public final int maxLevels;
        public final BigDecimal minDepth;
        public final BigDecimal tickSize;
        public final int bidSteps; // 正数更激进
        public final int askSteps; // 正数更保守
        public final BigDecimal quoteSize;
        public final String strategyName;

        public Params(BigDecimal targetQty, int maxLevels, BigDecimal minDepth,
                      BigDecimal tickSize, int bidSteps, int askSteps,
                      BigDecimal quoteSize, String strategyName) {
            this.targetQty = targetQty;
            this.maxLevels = maxLevels;
            this.minDepth = minDepth;
            this.tickSize = tickSize;
            this.bidSteps = bidSteps;
            this.askSteps = askSteps;
            this.quoteSize = quoteSize;
            this.strategyName = strategyName;
        }
    }

    private final Params p;

    public VMAPBestPriceStrategy(Params params) {
        this.p = params;
    }

    @Override
    public QuoteInstruction decideQuote(MarketDepthAggregator aggregator) {
        // 1) 合并各 provider 深度到单本盘口
        NavigableMap<BigDecimal, BigDecimal> mergedBid = mergeSide(aggregator.getAllBidDepth());
        NavigableMap<BigDecimal, BigDecimal> mergedAsk = mergeSide(aggregator.getAllAskDepth());

        if (mergedBid.isEmpty() || mergedAsk.isEmpty()) {
            return null;
        }

        // 2) 计算两侧 VWAP
        Optional<BigDecimal> vwapBid = vwapFromBook(mergedBid.descendingMap(), p.targetQty, p.maxLevels);
        Optional<BigDecimal> vwapAsk = vwapFromBook(mergedAsk, p.targetQty, p.maxLevels);

        if (vwapBid.isEmpty() || vwapAsk.isEmpty()) {
            return null;
        }

        // 3) 深度保护
        BigDecimal depthBid = cumulativeQty(mergedBid.descendingMap(), p.maxLevels);
        BigDecimal depthAsk = cumulativeQty(mergedAsk, p.maxLevels);
        if (depthBid.compareTo(p.minDepth) < 0 || depthAsk.compareTo(p.minDepth) < 0) {
            return null;
        }

        // 4) 在 VWAP 基础上做步长调整并对齐 tick
        BigDecimal bidPx = roundToTick(vwapBid.get().add(p.tickSize.multiply(BigDecimal.valueOf(p.bidSteps))), p.tickSize, true);
        BigDecimal askPx = roundToTick(vwapAsk.get().add(p.tickSize.multiply(BigDecimal.valueOf(p.askSteps))), p.tickSize, false);

        // 5) 价格带保护
        if (bidPx.compareTo(askPx) >= 0) {
            askPx = bidPx.add(p.tickSize);
        }

        return QuoteInstruction.createBidAskQuote(
                aggregator.getSymbol(), bidPx, askPx, p.quoteSize, p.strategyName
        );
    }

    private NavigableMap<BigDecimal, BigDecimal> mergeSide(Map<String, NavigableMap<BigDecimal, BigDecimal>> sideDepth) {
        NavigableMap<BigDecimal, BigDecimal> merged = new TreeMap<>();
        for (NavigableMap<BigDecimal, BigDecimal> perProvider : sideDepth.values()) {
            for (Map.Entry<BigDecimal, BigDecimal> e : perProvider.entrySet()) {
                merged.merge(e.getKey(), e.getValue(), BigDecimal::add);
            }
        }
        return merged;
    }

    private java.util.Optional<BigDecimal> vwapFromBook(NavigableMap<BigDecimal, BigDecimal> ordered,
                                              BigDecimal targetQty, int maxLevels) {
        if (ordered.isEmpty()) return java.util.Optional.empty();
        BigDecimal accQty = BigDecimal.ZERO;
        BigDecimal accNotional = BigDecimal.ZERO;
        int levels = 0;
        for (Map.Entry<BigDecimal, BigDecimal> e : ordered.entrySet()) {
            if (levels++ >= maxLevels) break;
            BigDecimal px = e.getKey();
            BigDecimal qty = e.getValue();
            BigDecimal take = qty;
            if (accQty.add(qty).compareTo(targetQty) > 0) {
                take = targetQty.subtract(accQty);
            }
            accQty = accQty.add(take);
            accNotional = accNotional.add(px.multiply(take));
            if (accQty.compareTo(targetQty) >= 0) break;
        }
        if (accQty.signum() == 0) return java.util.Optional.empty();
        return java.util.Optional.of(accNotional.divide(accQty, 10, RoundingMode.HALF_UP));
    }

    private BigDecimal cumulativeQty(NavigableMap<BigDecimal, BigDecimal> ordered, int maxLv) {
        BigDecimal sum = BigDecimal.ZERO;
        int i = 0;
        for (BigDecimal q : ordered.values()) {
            if (i++ >= maxLv) break;
            sum = sum.add(q);
        }
        return sum;
    }

    private BigDecimal roundToTick(BigDecimal px, BigDecimal tick, boolean isBid) {
        BigDecimal n = px.divide(tick, 0, isBid ? RoundingMode.FLOOR : RoundingMode.CEILING);
        return n.multiply(tick);
    }
}


