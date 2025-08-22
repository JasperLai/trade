package com.example.trade.demo.domain.fx.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.trade.demo.domain.fx.aggregate.FlatOrderAggregate;
import com.example.trade.demo.domain.fx.aggregate.FlatOrderId;

public final class InMemoryFlatOrderRepository implements FlatOrderRepository {
	private final Map<String, FlatOrderAggregate> store = new ConcurrentHashMap<>();

	@Override public void save(FlatOrderAggregate fo) { store.put(fo.id.value(), fo); }
	@Override public FlatOrderAggregate load(FlatOrderId id) { return store.get(id.value()); }
	@Override public void update(FlatOrderAggregate fo) { store.put(fo.id.value(), fo); }
}


