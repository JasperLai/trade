package com.example.trade.demo.domain.fx.aggregate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.example.trade.demo.domain.fx.valueobject.FxSymbol;
import com.example.trade.demo.domain.fx.valueobject.VwapParams.TargetType;
import com.example.trade.demo.domain.fx.valueobject.FlatSignal.Side;

public final class FlatOrderAggregate {

	public enum OrdStatus { PENDING_NEW, NEW, PARTIALLY_FILLED, FILLED, REJECTED, CANCELED, EXPIRED, REPLACED }

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

	public final FlatOrderId id;
	public final FxSymbol symbol;
	public final Side side;
	public final TargetType targetType;
	public final BigDecimal targetBase;
	public final BigDecimal targetQuote;
	public final List<ChildOrder> children = new ArrayList<>();

	public OrdStatus status = OrdStatus.PENDING_NEW;
	public BigDecimal cumBase = BigDecimal.ZERO;
	public BigDecimal cumQuote = BigDecimal.ZERO;

	public FlatOrderAggregate(FlatOrderId id, FxSymbol symbol, Side side, TargetType tt, BigDecimal target) {
		this.id = id;
		this.symbol = symbol;
		this.side = side;
		this.targetType = tt;
		this.targetBase  = (tt == TargetType.BASE_QTY) ? target : BigDecimal.ZERO;
		this.targetQuote = (tt == TargetType.QUOTE_NOTIONAL) ? target : BigDecimal.ZERO;
	}
}


