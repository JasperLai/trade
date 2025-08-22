package com.example.trade.demo.domain.fx.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.Consumer;

import com.example.trade.demo.domain.fx.aggregate.FlatOrderAggregate.ChildOrder;
import com.example.trade.demo.domain.fx.aggregate.FlatOrderAggregate.OrdStatus;

public final class OrderExecutionDomainService {

	public record ExecutionReport(String clientOrderId, String venueOrderId, OrdStatus ordStatus,
								  BigDecimal lastQtyBase, BigDecimal lastPx, Instant ts, String reason) {}

	public interface OmsClient {
		void submit(ChildOrder child, Consumer<ExecutionReport> onReport);
		void cancel(ChildOrder child, Consumer<ExecutionReport> onReport);
		void replace(ChildOrder child, BigDecimal newPx, Consumer<ExecutionReport> onReport);
	}

	public static final class InMemoryOms implements OmsClient {
		@Override public void submit(ChildOrder child, Consumer<ExecutionReport> onReport) {
			onReport.accept(new ExecutionReport(child.clientOrderId, "VENUE-" + child.clientOrderId, OrdStatus.NEW,
					BigDecimal.ZERO, child.price, Instant.now(), "ACK"));
			onReport.accept(new ExecutionReport(child.clientOrderId, "VENUE-" + child.clientOrderId, OrdStatus.PARTIALLY_FILLED,
					child.qtyBase, child.price, Instant.now(), "FILL"));
			onReport.accept(new ExecutionReport(child.clientOrderId, "VENUE-" + child.clientOrderId, OrdStatus.FILLED,
					BigDecimal.ZERO, child.price, Instant.now(), "DONE"));
		}
		@Override public void cancel(ChildOrder child, Consumer<ExecutionReport> onReport) {
			onReport.accept(new ExecutionReport(child.clientOrderId, child.venueOrderId, OrdStatus.CANCELED,
					BigDecimal.ZERO, child.price, Instant.now(), "CXL"));
		}
		@Override public void replace(ChildOrder child, BigDecimal newPx, Consumer<ExecutionReport> onReport) {
			onReport.accept(new ExecutionReport(child.clientOrderId, child.venueOrderId, OrdStatus.REPLACED,
					BigDecimal.ZERO, newPx, Instant.now(), "RPL"));
		}
	}
}


