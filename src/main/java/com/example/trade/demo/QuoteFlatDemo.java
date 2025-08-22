package com.example.trade.demo;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import com.example.trade.demo.domain.entity.MarketDepthAggregator;
import com.example.trade.demo.domain.entity.OrderBookLevel;
import com.example.trade.demo.domain.fx.aggregate.FlatOrderAggregate;
import com.example.trade.demo.domain.fx.aggregate.FlatOrderId;
import com.example.trade.demo.domain.fx.application.FlatOrderApplicationService;
import com.example.trade.demo.domain.fx.application.MarketDepthAdapters.FromLegacyAggregator;
import com.example.trade.demo.domain.fx.repository.FlatOrderRepository;
import com.example.trade.demo.domain.fx.repository.InMemoryFlatOrderRepository;
import com.example.trade.demo.domain.fx.service.OrderExecutionDomainService.InMemoryOms;
import com.example.trade.demo.domain.fx.service.VwapSplittingDomainService;
import com.example.trade.demo.domain.fx.valueobject.FlatSignal;
import com.example.trade.demo.domain.fx.valueobject.FxSymbol;
import com.example.trade.demo.domain.fx.valueobject.FxSymbolRule;
import com.example.trade.demo.domain.fx.valueobject.VwapParams;

public class QuoteFlatDemo {

	public static void main(String[] args) throws Exception {
		// 1) 准备一个现有的 MarketDepthAggregator 并填充示例数据
		MarketDepthAggregator agg = new MarketDepthAggregator("EURUSD");
		agg.updateDepth("LP1",
				List.of(new OrderBookLevel(new BigDecimal("1.10000"), new BigDecimal("2")),
						new OrderBookLevel(new BigDecimal("1.10010"), new BigDecimal("3"))),
				List.of(new OrderBookLevel(new BigDecimal("1.09990"), new BigDecimal("2")),
						new OrderBookLevel(new BigDecimal("1.09980"), new BigDecimal("3"))));
		agg.updateDepth("LP2",
				List.of(new OrderBookLevel(new BigDecimal("1.10005"), new BigDecimal("1.5"))),
				List.of(new OrderBookLevel(new BigDecimal("1.09995"), new BigDecimal("1.2"))));

		FxSymbol symbol = new FxSymbol("EUR", "USD");
		FxSymbolRule rule = new FxSymbolRule(new BigDecimal("0.00005"), 5, 2, 2);
		FromLegacyAggregator md = new FromLegacyAggregator(agg, rule);

		// 2) 准备执行环境
		InMemoryOms oms = new InMemoryOms();
		FlatOrderRepository repo = new InMemoryFlatOrderRepository();
		ScheduledExecutorService timer = FlatOrderApplicationService.newTimer();
		FlatOrderApplicationService app = new FlatOrderApplicationService(md, new VwapSplittingDomainService(), oms, repo, timer);

		// 3) 触发一个 BUY Base 按 Base 数量目标的平盘
		FlatSignal sig = new FlatSignal(symbol, FlatSignal.Side.BUY, new BigDecimal("3.5"), "rebalance", Instant.now());
		VwapParams p = new VwapParams(symbol, VwapParams.TargetType.BASE_QTY, new BigDecimal("3.5"),
				VwapParams.ExecutionIntent.AUTO, Duration.ofSeconds(2), new BigDecimal("0.00010"));
		FlatOrderId id = app.start(sig, p);
		System.out.println("Started flatId=" + id.value());

		Thread.sleep(1000);
		// 4) 输出父单聚合结果
		FlatOrderAggregate fo = repo.load(id);
		System.out.println("Parent status=" + fo.status + ", cumBase=" + fo.cumBase + ", cumQuote=" + fo.cumQuote);
	}
}


