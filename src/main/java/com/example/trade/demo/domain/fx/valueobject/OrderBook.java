package com.example.trade.demo.domain.fx.valueobject;

import java.math.BigDecimal;
import java.util.List;

public final class OrderBook {
	public record ProviderDepth(String providerId, BigDecimal baseQty) {}
	public record PriceLevel(BigDecimal price, List<ProviderDepth> providers) {}

	private final List<PriceLevel> asksAsc;
	private final List<PriceLevel> bidsDesc;

	public OrderBook(List<PriceLevel> asksAsc, List<PriceLevel> bidsDesc) {
		this.asksAsc = asksAsc; this.bidsDesc = bidsDesc;
	}
	public List<PriceLevel> asksAsc() { return asksAsc; }
	public List<PriceLevel> bidsDesc() { return bidsDesc; }
}


