package com.example.trade.demo.domain.fx;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.example.trade.demo.domain.fx.FxTypes.*;

public final class FlatModels {

	public record FlatSignal(FxSymbol symbol, Side side, BigDecimal suggestedTarget, String reason, Instant ts) {}

	public record VwapParams(
			FxSymbol symbol,
			TargetType targetType,
			BigDecimal targetValue,
			ExecutionIntent intent,
			Duration ttl,
			BigDecimal slippageBuffer
	) {}

	public static final class ChildOrder {
		public final String clientOrderId;
		public final FxSymbol symbol;
		public final Side side;
		public final BigDecimal price;     // Quote/Base
		public final BigDecimal qtyBase;   // Base
		public final String providerId;

		public OrdStatus status = OrdStatus.PENDING_NEW;
		public BigDecimal cumBase = BigDecimal.ZERO;
		public String venueOrderId;
		public Instant submitTs;

		public ChildOrder(String clientOrderId, FxSymbol symbol, Side side,
					   BigDecimal price, BigDecimal qtyBase, String providerId) {
			this.clientOrderId = clientOrderId;
			this.symbol = symbol;
			this.side = side;
			this.price = price;
			this.qtyBase = qtyBase;
			this.providerId = providerId;
		}
	}

	public static final class FlatOrder {
		public final String flatOrderId;
		public final FxSymbol symbol;
		public final Side side;
		public final TargetType targetType;
		public final BigDecimal targetBase;
		public final BigDecimal targetQuote;
		public final List<ChildOrder> children = new ArrayList<>();

		public OrdStatus status = OrdStatus.PENDING_NEW;
		public BigDecimal cumBase = BigDecimal.ZERO;
		public BigDecimal cumQuote = BigDecimal.ZERO;

		public FlatOrder(String id, FxSymbol symbol, Side side, TargetType tt, BigDecimal target) {
			this.flatOrderId = id;
			this.symbol = symbol;
			this.side = side;
			this.targetType = tt;
			this.targetBase  = (tt == TargetType.BASE_QTY) ? target : BigDecimal.ZERO;
			this.targetQuote = (tt == TargetType.QUOTE_NOTIONAL) ? target : BigDecimal.ZERO;
		}
	}
}


