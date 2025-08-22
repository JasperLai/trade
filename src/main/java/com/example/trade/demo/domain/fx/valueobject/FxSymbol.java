package com.example.trade.demo.domain.fx.valueobject;

/** 外汇符号：Base/Quote 采用 ISO 货币代码 */
public record FxSymbol(String base, String quote) {
	@Override public String toString() { return base + "/" + quote; }
}


