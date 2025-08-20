package com.example.trade.demo.domain.fx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.trade.demo.domain.fx.FxTypes.FxSymbol;
import com.example.trade.demo.domain.fx.FxTypes.FxSymbolRule;
import com.example.trade.demo.domain.fx.FxTypes.Side;
import com.example.trade.demo.domain.fx.FlatModels.ChildOrder;
import com.example.trade.demo.domain.fx.FlatModels.VwapParams;
import com.example.trade.demo.domain.fx.OrderBookModels.OrderBook;
import com.example.trade.demo.domain.fx.OrderBookModels.PriceLevel;
import com.example.trade.demo.domain.fx.OrderBookModels.ProviderDepth;
import com.example.trade.demo.domain.fx.FxTypes.ExecutionIntent;
import com.example.trade.demo.domain.fx.FxTypes.TargetType;

public interface VwapStrategy {
	List<ChildOrder> build(FxSymbol symbol, Side side, VwapParams params,
						 OrderBook book, FxSymbolRule rule, String flatOrderId);

	/** FX 专用 VWAP 拆单 */
	final class FxVwapStrategy implements VwapStrategy {
		@Override
		public List<ChildOrder> build(FxSymbol s, Side side, VwapParams p,
									 OrderBook book, FxSymbolRule r, String flatId) {
			List<ChildOrder> out = new ArrayList<>();
			switch (side) {
				case BUY -> {
					if (p.targetType() == TargetType.BASE_QTY) {
						fillByBaseTarget(out, s, side, p.targetValue(), book.asksAsc(), r, true);
					} else {
						if (p.intent() == ExecutionIntent.TAKER)
							fillByQuoteBudgetOnAsk(out, s, side, p.targetValue(), book.asksAsc(), r);
						else
							fillByQuoteBudgetOnBid(out, s, side, p.targetValue(), book.bidsDesc(), r);
					}
				}
				case SELL -> {
					if (p.targetType() == TargetType.BASE_QTY) {
						fillByBaseTarget(out, s, side, p.targetValue(), book.bidsDesc(), r, false);
					} else {
						if (p.intent() == ExecutionIntent.TAKER)
							fillByQuoteBudgetOnBid(out, s, side, p.targetValue(), book.bidsDesc(), r);
						else
							fillByQuoteBudgetOnAsk(out, s, side, p.targetValue(), book.asksAsc(), r);
					}
				}
			}
			return out;
		}

		private void fillByBaseTarget(List<ChildOrder> out, FxSymbol s, Side side, BigDecimal baseTarget,
								   List<PriceLevel> levels, FxSymbolRule r, boolean isAsk) {
			BigDecimal remain = baseTarget;
			for (PriceLevel lvl : levels) {
				BigDecimal px = alignPx(lvl.price(), r, !isAsk);
				for (ProviderDepth pd : lvl.providers()) {
					if (remain.signum() <= 0) break;
					BigDecimal takeBase = pd.baseQty().min(remain).max(BigDecimal.ZERO);
					if (takeBase.signum() <= 0) continue;
					out.add(new ChildOrder(UUID.randomUUID().toString(), s, side, px,
							scaleQty(takeBase, r), pd.providerId()));
					remain = remain.subtract(takeBase);
				}
				if (remain.signum() <= 0) break;
			}
		}

		private void fillByQuoteBudgetOnBid(List<ChildOrder> out, FxSymbol s, Side side, BigDecimal quoteBudget,
										 List<PriceLevel> bidsDesc, FxSymbolRule r) {
			BigDecimal qRemain = quoteBudget;
			for (PriceLevel lvl : bidsDesc) {
				BigDecimal px = alignPx(lvl.price(), r, true);
				for (ProviderDepth pd : lvl.providers()) {
					if (qRemain.signum() <= 0) break;
					BigDecimal quoteAvail = pd.baseQty().multiply(px);
					BigDecimal takeQuote = quoteAvail.min(qRemain).max(BigDecimal.ZERO);
					if (takeQuote.signum() <= 0) continue;
					BigDecimal takeBase = safeDiv(takeQuote, px, r.baseQtyScale());
					out.add(new ChildOrder(UUID.randomUUID().toString(), s, side, px,
							scaleQty(takeBase, r), pd.providerId()));
					qRemain = qRemain.subtract(takeQuote);
				}
				if (qRemain.signum() <= 0) break;
			}
		}

		private void fillByQuoteBudgetOnAsk(List<ChildOrder> out, FxSymbol s, Side side, BigDecimal quoteBudget,
										 List<PriceLevel> asksAsc, FxSymbolRule r) {
			BigDecimal qRemain = quoteBudget;
			for (PriceLevel lvl : asksAsc) {
				BigDecimal px = alignPx(lvl.price(), r, false);
				for (ProviderDepth pd : lvl.providers()) {
					if (qRemain.signum() <= 0) break;
					BigDecimal quoteAvail = pd.baseQty().multiply(px);
					BigDecimal takeQuote = quoteAvail.min(qRemain).max(BigDecimal.ZERO);
					if (takeQuote.signum() <= 0) continue;
					BigDecimal takeBase = safeDiv(takeQuote, px, r.baseQtyScale());
					out.add(new ChildOrder(UUID.randomUUID().toString(), s, side, px,
							scaleQty(takeBase, r), pd.providerId()));
					qRemain = qRemain.subtract(takeQuote);
				}
				if (qRemain.signum() <= 0) break;
			}
		}

		private BigDecimal alignPx(BigDecimal px, FxSymbolRule r, boolean isBid) {
			BigDecimal n = px.divide(r.tickSize(), 0, isBid ? RoundingMode.FLOOR : RoundingMode.CEILING);
			return n.multiply(r.tickSize()).setScale(r.priceScale(), RoundingMode.HALF_UP);
		}
		private BigDecimal scaleQty(BigDecimal q, FxSymbolRule r) {
			return q.setScale(r.baseQtyScale(), RoundingMode.DOWN);
		}
		private BigDecimal safeDiv(BigDecimal a, BigDecimal b, int scale) {
			return (b.signum() == 0) ? BigDecimal.ZERO.setScale(scale) : a.divide(b, scale, RoundingMode.HALF_UP);
		}
	}
}


