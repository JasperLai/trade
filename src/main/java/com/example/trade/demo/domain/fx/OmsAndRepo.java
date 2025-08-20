package com.example.trade.demo.domain.fx;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.example.trade.demo.domain.fx.FxTypes.OrdStatus;
import com.example.trade.demo.domain.fx.FlatModels.ChildOrder;
import com.example.trade.demo.domain.fx.FlatModels.FlatOrder;
import com.example.trade.demo.domain.fx.FxTypes.FxSymbol;
import com.example.trade.demo.domain.fx.FxTypes.Side;

public final class OmsAndRepo {

	public record ExecutionReport(
			String clientOrderId, String venueOrderId, OrdStatus ordStatus,
			BigDecimal lastQtyBase, BigDecimal lastPx, Instant ts, String reason) {}

	public interface OmsClient {
		void submit(ChildOrder child, Consumer<ExecutionReport> onReport);
		void cancel(ChildOrder child, Consumer<ExecutionReport> onReport);
		void replace(ChildOrder child, BigDecimal newPx, Consumer<ExecutionReport> onReport);
	}

	/** 极简内存 OMS：提交即回报 NEW，随后模拟部分成交 */
	public static final class InMemoryOms implements OmsClient {
		@Override
		public void submit(ChildOrder child, Consumer<ExecutionReport> onReport) {
			onReport.accept(new ExecutionReport(child.clientOrderId, "VENUE-" + child.clientOrderId,
					OrdStatus.NEW, BigDecimal.ZERO, child.price, Instant.now(), "ACK"));
			// 模拟一次立即成交
			onReport.accept(new ExecutionReport(child.clientOrderId, "VENUE-" + child.clientOrderId,
					OrdStatus.PARTIALLY_FILLED, child.qtyBase, child.price, Instant.now(), "FILL"));
			onReport.accept(new ExecutionReport(child.clientOrderId, "VENUE-" + child.clientOrderId,
					OrdStatus.FILLED, BigDecimal.ZERO, child.price, Instant.now(), "DONE"));
		}

		@Override
		public void cancel(ChildOrder child, Consumer<ExecutionReport> onReport) {
			onReport.accept(new ExecutionReport(child.clientOrderId, child.venueOrderId,
					OrdStatus.CANCELED, BigDecimal.ZERO, child.price, Instant.now(), "CXL"));
		}

		@Override
		public void replace(ChildOrder child, BigDecimal newPx, Consumer<ExecutionReport> onReport) {
			onReport.accept(new ExecutionReport(child.clientOrderId, child.venueOrderId,
					OrdStatus.REPLACED, BigDecimal.ZERO, newPx, Instant.now(), "RPL"));
		}
	}

	public interface FlatOrderRepository {
		void save(FlatOrder fo);
		void saveChild(String flatId, ChildOrder co);
		FlatOrder load(String flatId);
		ChildOrder loadChild(String flatId, String clientOrderId);
		void update(FlatOrder fo);
		void updateChild(String flatId, ChildOrder co);
	}

	public static final class InMemoryFlatRepo implements FlatOrderRepository {
		private final Map<String, FlatOrder> parents = new ConcurrentHashMap<>();
		private final Map<String, Map<String, ChildOrder>> children = new ConcurrentHashMap<>();

		@Override public void save(FlatOrder fo) { parents.put(fo.flatOrderId, fo); }
		@Override public void saveChild(String flatId, ChildOrder co) {
			children.computeIfAbsent(flatId, k -> new ConcurrentHashMap<>()).put(co.clientOrderId, co);
		}
		@Override public FlatOrder load(String flatId) { return parents.get(flatId); }
		@Override public ChildOrder loadChild(String flatId, String clientOrderId) {
			Map<String, ChildOrder> m = children.get(flatId); return m == null ? null : m.get(clientOrderId);
		}
		@Override public void update(FlatOrder fo) { parents.put(fo.flatOrderId, fo); }
		@Override public void updateChild(String flatId, ChildOrder co) { saveChild(flatId, co); }
	}

	public interface RiskGateway {
		void onFlatDone(FxSymbol symbol, Side side, BigDecimal cumBase, BigDecimal cumQuote, BigDecimal realizedVWAP);
	}

	public static final class LogRiskGateway implements RiskGateway {
		@Override
		public void onFlatDone(FxSymbol symbol, Side side, BigDecimal cumBase, BigDecimal cumQuote, BigDecimal realizedVWAP) {
			System.out.println("[Risk] Flat Done: sym=" + symbol + ", side=" + side +
					", base=" + cumBase + ", quote=" + cumQuote + ", vwap=" + realizedVWAP);
		}
	}
}


