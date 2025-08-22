package com.example.trade.demo.domain.fx.repository;

import com.example.trade.demo.domain.fx.aggregate.FlatOrderAggregate;
import com.example.trade.demo.domain.fx.aggregate.FlatOrderId;

public interface FlatOrderRepository {
	void save(FlatOrderAggregate fo);
	FlatOrderAggregate load(FlatOrderId id);
	void update(FlatOrderAggregate fo);
}


