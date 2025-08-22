package com.example.trade.demo.domain.fx.event;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.trade.demo.domain.fx.aggregate.FlatOrderId;

public record ChildOrderExecuted(FlatOrderId flatOrderId, String clientOrderId,
								  BigDecimal lastQtyBase, BigDecimal lastPx, Instant occurredOn) implements DomainEvent {}


