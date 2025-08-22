package com.example.trade.demo.domain.fx.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import com.example.trade.demo.domain.entity.MarketDepthAggregator;
import com.example.trade.demo.domain.fx.valueobject.FxSymbol;
import com.example.trade.demo.domain.fx.valueobject.FxSymbolRule;
import com.example.trade.demo.domain.fx.valueobject.OrderBook;
import com.example.trade.demo.domain.fx.valueobject.OrderBook.PriceLevel;
import com.example.trade.demo.domain.fx.valueobject.OrderBook.ProviderDepth;

public final class MarketDepthAdapters {

	public static final class FromLegacyAggregator implements FlatOrderApplicationService.MarketDepthProvider {
		private final MarketDepthAggregator agg;
		private final FxSymbolRule rule;

		public FromLegacyAggregator(MarketDepthAggregator agg, FxSymbolRule rule) {
			this.agg = agg; this.rule = rule;
		}

		@Override public OrderBook latest(FxSymbol symbol) {
			List<PriceLevel> asks = merge(agg.getAllAskDepth(), true);
			List<PriceLevel> bids = merge(agg.getAllBidDepth(), false);
			return new OrderBook(asks, bids);
		}
		@Override public FxSymbolRule ruleOf(FxSymbol symbol) { return rule; }

		private List<PriceLevel> merge(Map<String, NavigableMap<BigDecimal, BigDecimal>> side, boolean isAsk) {
			Map<BigDecimal, List<ProviderDepth>> tmp = new java.util.HashMap<>();
			for (Map.Entry<String, NavigableMap<BigDecimal, BigDecimal>> e : side.entrySet()) {
				String provider = e.getKey();
				for (Map.Entry<BigDecimal, BigDecimal> lv : e.getValue().entrySet()) {
					BigDecimal px = lv.getKey();
					BigDecimal qty = lv.getValue();
					tmp.computeIfAbsent(px, k -> new ArrayList<>()).add(new ProviderDepth(provider, qty));
				}
			}
			List<PriceLevel> out = new ArrayList<>();
			for (Map.Entry<BigDecimal, List<ProviderDepth>> e : tmp.entrySet()) {
				out.add(new PriceLevel(e.getKey(), e.getValue()));
			}
			out.sort(Comparator.comparing(PriceLevel::price));
			if (!isAsk) java.util.Collections.reverse(out);
			return out;
		}
	}
}


