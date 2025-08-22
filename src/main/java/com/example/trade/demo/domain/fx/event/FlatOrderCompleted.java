package com.example.trade.demo.domain.fx.event;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.trade.demo.domain.fx.aggregate.FlatOrderId;

public record FlatOrderCompleted(FlatOrderId flatOrderId, BigDecimal cumBase, BigDecimal cumQuote,
								  BigDecimal realizedVWAP, Instant occurredOn) implements DomainEvent {}


