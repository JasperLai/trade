package com.example.trade.demo.domain.fx.valueobject;

import java.math.BigDecimal;

/** 符号规则：价格单位=Quote/Base；数量单位=Base */
public record FxSymbolRule(
		BigDecimal tickSize,
		int priceScale,
		int baseQtyScale,
		int quoteNotionalScale
) {}


