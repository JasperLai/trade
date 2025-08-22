package com.example.trade.demo.domain.fx.application;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.example.trade.demo.domain.fx.aggregate.FlatOrderAggregate;
import com.example.trade.demo.domain.fx.aggregate.FlatOrderAggregate.ChildOrder;
import com.example.trade.demo.domain.fx.aggregate.FlatOrderAggregate.OrdStatus;
import com.example.trade.demo.domain.fx.aggregate.FlatOrderId;
import com.example.trade.demo.domain.fx.event.ChildOrderExecuted;
import com.example.trade.demo.domain.fx.event.FlatOrderCompleted;
import com.example.trade.demo.domain.fx.event.FlatOrderStarted;
import com.example.trade.demo.domain.fx.repository.FlatOrderRepository;
import com.example.trade.demo.domain.fx.service.OrderExecutionDomainService.ExecutionReport;
import com.example.trade.demo.domain.fx.service.OrderExecutionDomainService.OmsClient;
import com.example.trade.demo.domain.fx.service.VwapSplittingDomainService;
import com.example.trade.demo.domain.fx.valueobject.FxSymbolRule;
import com.example.trade.demo.domain.fx.valueobject.OrderBook;
import com.example.trade.demo.domain.fx.valueobject.VwapParams;
import com.example.trade.demo.domain.fx.valueobject.FlatSignal;

public final class FlatOrderApplicationService {

	public interface MarketDepthProvider {
		OrderBook latest(com.example.trade.demo.domain.fx.valueobject.FxSymbol symbol);
		FxSymbolRule ruleOf(com.example.trade.demo.domain.fx.valueobject.FxSymbol symbol);
	}

	private final MarketDepthProvider md;
	private final VwapSplittingDomainService splitter;
	private final OmsClient oms;
	private final FlatOrderRepository repo;
	private final ScheduledExecutorService timer;
	private final Duration ackTimeout = Duration.ofMillis(1500);
	private final Duration parentTimeout = Duration.ofSeconds(5);

	public FlatOrderApplicationService(MarketDepthProvider md, VwapSplittingDomainService splitter,
									   OmsClient oms, FlatOrderRepository repo, ScheduledExecutorService timer) {
		this.md = md; this.splitter = splitter; this.oms = oms; this.repo = repo; this.timer = timer;
	}

	public FlatOrderId start(FlatSignal sig, VwapParams params) {
		OrderBook book = md.latest(params.symbol());
		FxSymbolRule rule = md.ruleOf(params.symbol());

		FlatOrderId id = FlatOrderId.newId();
		FlatOrderAggregate flat = new FlatOrderAggregate(id, params.symbol(), sig.side(), params.targetType(), params.targetValue());
		repo.save(flat);

		// 领域事件：开始
		emit(new FlatOrderStarted(id, params.symbol(), sig.side(), params.targetType(), params.targetValue(), Instant.now()));

		List<ChildOrder> children = splitter.split(params.symbol(), sig.side(), params, book, rule);
		flat.children.addAll(children);
		repo.update(flat);
		for (ChildOrder c : children) submitChild(flat, c, rule);
		timer.schedule(() -> onParentTimeout(id), parentTimeout.toMillis(), TimeUnit.MILLISECONDS);
		return id;
	}

	private void submitChild(FlatOrderAggregate flat, ChildOrder c, FxSymbolRule rule) {
		c.submitTs = Instant.now();
		timer.schedule(() -> onAckTimeout(flat.id, c.clientOrderId), ackTimeout.toMillis(), TimeUnit.MILLISECONDS);
		oms.submit(c, rpt -> onReport(flat.id, rpt, rule));
	}

	private void onReport(FlatOrderId id, ExecutionReport rpt, FxSymbolRule rule) {
		FlatOrderAggregate fo = repo.load(id);
		if (fo == null) return;
		FlatOrderAggregate.ChildOrder co = fo.children.stream().filter(x -> x.clientOrderId.equals(rpt.clientOrderId())).findFirst().orElse(null);
		if (co == null) return;

		co.venueOrderId = rpt.venueOrderId();
		co.status = rpt.ordStatus();

		if (rpt.lastQtyBase() != null && rpt.lastQtyBase().signum() > 0) {
			BigDecimal incBase = rpt.lastQtyBase();
			BigDecimal incQuote = incBase.multiply(rpt.lastPx());
			co.cumBase = co.cumBase.add(incBase);
			fo.cumBase = fo.cumBase.add(incBase);
			fo.cumQuote = fo.cumQuote.add(incQuote);
			emit(new ChildOrderExecuted(id, co.clientOrderId, incBase, rpt.lastPx(), Instant.now()));
		}
		repo.update(fo);

		if (targetMet(fo) || allTerminal(fo)) {
			cancelRemainders(fo);
			finish(fo, rule);
		}
	}

	private void onAckTimeout(FlatOrderId id, String clientOrderId) {
		FlatOrderAggregate fo = repo.load(id);
		if (fo == null) return;
		FlatOrderAggregate.ChildOrder co = fo.children.stream().filter(x -> x.clientOrderId.equals(clientOrderId)).findFirst().orElse(null);
		if (co != null && co.status == OrdStatus.PENDING_NEW) {
			oms.cancel(co, rpt -> {});
		}
	}

	private void onParentTimeout(FlatOrderId id) {
		FlatOrderAggregate fo = repo.load(id);
		if (fo == null || fo.status == OrdStatus.FILLED || fo.status == OrdStatus.CANCELED) return;
		if (!targetMet(fo)) { cancelRemainders(fo); finish(fo, md.ruleOf(fo.symbol)); }
	}

	private void cancelRemainders(FlatOrderAggregate fo) {
		fo.children.stream().filter(c ->
				c.status == OrdStatus.NEW || c.status == OrdStatus.PARTIALLY_FILLED || c.status == OrdStatus.PENDING_NEW
		).forEach(c -> oms.cancel(c, rpt -> {}));
	}

	private void finish(FlatOrderAggregate fo, FxSymbolRule rule) {
		fo.status = OrdStatus.FILLED; // 简化
		repo.update(fo);
		BigDecimal vwap = fo.cumBase.signum() == 0 ? BigDecimal.ZERO : fo.cumQuote.divide(fo.cumBase, rule.priceScale(), java.math.RoundingMode.HALF_UP);
		emit(new FlatOrderCompleted(fo.id, fo.cumBase, fo.cumQuote, vwap, Instant.now()));
	}

	private boolean targetMet(FlatOrderAggregate f) {
		return (f.targetType == VwapParams.TargetType.BASE_QTY)
				? f.cumBase.compareTo(f.targetBase) >= 0
				: f.cumQuote.compareTo(f.targetQuote) >= 0;
	}
	private boolean allTerminal(FlatOrderAggregate f) {
		return f.children.stream().allMatch(c ->
				switch (c.status) { case FILLED, REJECTED, CANCELED, EXPIRED -> true; default -> false; });
	}

	private void emit(Object evt) {
		// 简化：控制台打印；生产可对接事件总线
		System.out.println("[DomainEvent] " + evt);
	}

	public static ScheduledExecutorService newTimer() {
		return new ScheduledThreadPoolExecutor(1, r -> {
			Thread t = new Thread(r, "FlatOrderAppServiceTimer"); t.setDaemon(true); return t;
		});
	}
}


