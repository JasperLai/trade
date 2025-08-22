package com.example.trade.demo.domain.fx;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.trade.demo.domain.fx.FxTypes.*;
import com.example.trade.demo.domain.fx.FlatModels.*;
import com.example.trade.demo.domain.fx.OrderBookModels.*;
import com.example.trade.demo.domain.fx.OmsAndRepo.*;

public final class FxFlatCoordinator {

	public interface MarketDepthAggregatorFx {
		OrderBook latest(FxSymbol symbol);
		FxSymbolRule ruleOf(FxSymbol symbol);
	}

	private final MarketDepthAggregatorFx md;
	private final VwapStrategy vwap;
	private final OmsClient oms;
	private final FlatOrderRepository repo;
	private final RiskGateway risk;
	private final ScheduledExecutorService timer;
	private final Duration ackTimeout = Duration.ofMillis(1500);
	private final Duration parentTimeout = Duration.ofSeconds(5);

	public FxFlatCoordinator(MarketDepthAggregatorFx md, VwapStrategy vwap, OmsClient oms,
							 FlatOrderRepository repo, RiskGateway risk, ScheduledExecutorService timer) {
		this.md = md; this.vwap = vwap; this.oms = oms; this.repo = repo; this.risk = risk; this.timer = timer;
	}

	public String start(FlatSignal sig, VwapParams params) {
		OrderBook book = md.latest(params.symbol());
		FxSymbolRule rule = md.ruleOf(params.symbol());

		FlatOrder flat = new FlatOrder(UUID.randomUUID().toString(), params.symbol(), sig.side(),
									  params.targetType(), params.targetValue());
		flat.children.addAll(vwap.build(params.symbol(), sig.side(), params, book, rule, flat.flatOrderId));
		repo.save(flat);
		for (ChildOrder c : flat.children) submitChild(flat, c, rule);
		timer.schedule(() -> onParentTimeout(flat.flatOrderId), parentTimeout.toMillis(), TimeUnit.MILLISECONDS);
		return flat.flatOrderId;
	}

	private void submitChild(FlatOrder flat, ChildOrder c, FxSymbolRule rule) {
		c.submitTs = Instant.now();
		repo.saveChild(flat.flatOrderId, c);
		timer.schedule(() -> onAckTimeout(flat.flatOrderId, c.clientOrderId), ackTimeout.toMillis(), TimeUnit.MILLISECONDS);
		oms.submit(c, rpt -> onReport(flat.flatOrderId, rpt, rule));
	}

	private void onReport(String flatId, ExecutionReport rpt, FxSymbolRule rule) {
		FlatOrder fo = repo.load(flatId);
		ChildOrder co = repo.loadChild(flatId, rpt.clientOrderId());
		if (fo == null || co == null) return;

		co.venueOrderId = rpt.venueOrderId();
		co.status = rpt.ordStatus();

		if (rpt.lastQtyBase() != null && rpt.lastQtyBase().signum() > 0) {
			BigDecimal incBase = rpt.lastQtyBase();
			BigDecimal incQuote = incBase.multiply(rpt.lastPx());
			co.cumBase = co.cumBase.add(incBase);
			fo.cumBase = fo.cumBase.add(incBase);
			fo.cumQuote = fo.cumQuote.add(incQuote);
		}
		repo.updateChild(flatId, co);
		repo.update(fo);

		if (targetMet(fo) || allTerminal(fo)) {
			cancelRemainders(fo);
			finish(fo, rule);
		}
	}

	private void onAckTimeout(String flatId, String clientOrderId) {
		ChildOrder co = repo.loadChild(flatId, clientOrderId);
		if (co != null && co.status == OrdStatus.PENDING_NEW) {
			oms.cancel(co, rpt -> {});
		}
	}

	private void onParentTimeout(String flatId) {
		FlatOrder fo = repo.load(flatId);
		if (fo == null || fo.status == OrdStatus.FILLED || fo.status == OrdStatus.CANCELED) return;
		if (!targetMet(fo)) { cancelRemainders(fo); finish(fo, md.ruleOf(fo.symbol)); }
	}

	private void cancelRemainders(FlatOrder fo) {
		fo.children.stream().filter(c ->
				c.status == OrdStatus.NEW || c.status == OrdStatus.PARTIALLY_FILLED || c.status == OrdStatus.PENDING_NEW
		).forEach(c -> oms.cancel(c, rpt -> {}));
	}

	private void finish(FlatOrder fo, FxSymbolRule rule) {
		fo.status = OrdStatus.FILLED; // 简化
		repo.update(fo);
		risk.onFlatDone(fo.symbol, fo.side, fo.cumBase, fo.cumQuote, realizedVWAP(fo, rule));
	}

	private boolean targetMet(FlatOrder f) {
		return (f.targetType == TargetType.BASE_QTY)
				? f.cumBase.compareTo(f.targetBase) >= 0
				: f.cumQuote.compareTo(f.targetQuote) >= 0;
	}
	private boolean allTerminal(FlatOrder f) {
		return f.children.stream().allMatch(c ->
				switch (c.status) { case FILLED, REJECTED, CANCELED, EXPIRED -> true; default -> false; });
	}

	private BigDecimal realizedVWAP(FlatOrder f, FxSymbolRule rule) {
		return f.cumBase.signum() == 0 ? BigDecimal.ZERO
				: f.cumQuote.divide(f.cumBase, rule.priceScale(), java.math.RoundingMode.HALF_UP);
	}

	public static ScheduledExecutorService newTimer() {
		return new ScheduledThreadPoolExecutor(1, r -> {
			Thread t = new Thread(r, "FxFlatCoordinatorTimer");
			t.setDaemon(true);
			return t;
		});
	}
}


