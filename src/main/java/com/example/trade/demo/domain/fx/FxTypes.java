package com.example.trade.demo.domain.fx;

import java.math.BigDecimal;

/**
 * FX 基础类型与通用枚举。
 */
public final class FxTypes {

	public enum Side { BUY, SELL }

	public enum TargetType { BASE_QTY, QUOTE_NOTIONAL }

	public enum ExecutionIntent { MAKER, TAKER, AUTO }

	public enum OrdStatus {
		PENDING_NEW, NEW, PARTIALLY_FILLED, FILLED, REJECTED, CANCELED, EXPIRED, REPLACED
	}

	/** 外汇符号：Base/Quote 采用 ISO 货币代码 */
	public record FxSymbol(String base, String quote) {
		@Override public String toString() { return base + "/" + quote; }
	}

	/** 符号规则：价格单位=Quote/Base；数量单位=Base */
	public record FxSymbolRule(
			BigDecimal tickSize,
			int priceScale,
			int baseQtyScale,
			int quoteNotionalScale
	) {}
}


