package com.example.trade.demo.domain.fx.valueobject;

import java.math.BigDecimal;
import java.time.Instant;

/** 平盘触发信号（来自风控/持仓） */
public record FlatSignal(FxSymbol symbol, Side side, BigDecimal suggestedTarget, String reason, Instant ts) {
	public enum Side { BUY, SELL }
}


