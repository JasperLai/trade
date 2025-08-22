package com.example.trade.demo.domain.fx.event;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.trade.demo.domain.fx.aggregate.FlatOrderId;
import com.example.trade.demo.domain.fx.valueobject.FxSymbol;
import com.example.trade.demo.domain.fx.valueobject.VwapParams.TargetType;
import com.example.trade.demo.domain.fx.valueobject.FlatSignal.Side;

public record FlatOrderStarted(FlatOrderId flatOrderId, FxSymbol symbol, Side side,
								 TargetType targetType, BigDecimal targetValue, Instant occurredOn) implements DomainEvent {}


