package com.example.trade.demo.domain.fx;

import java.math.BigDecimal;
import java.util.List;

public final class OrderBookModels {

	public record ProviderDepth(String providerId, BigDecimal baseQty) {}

	/** price 单位: Quote/Base；数量单位: Base */
	public record PriceLevel(BigDecimal price, List<ProviderDepth> providers) {}

	/** asksAsc: 从低到高；bidsDesc: 从高到低 */
	public record OrderBook(List<PriceLevel> asksAsc, List<PriceLevel> bidsDesc) {}
}


