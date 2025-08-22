package com.example.trade.demo.domain.fx.valueobject;

import java.math.BigDecimal;
import java.time.Duration;

public record VwapParams(
		FxSymbol symbol,
		TargetType targetType,
		BigDecimal targetValue,
		ExecutionIntent intent,
		Duration ttl,
		BigDecimal slippageBuffer
) {
	public enum TargetType { BASE_QTY, QUOTE_NOTIONAL }
	public enum ExecutionIntent { MAKER, TAKER, AUTO }
}


