package com.example.trade.demo;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;

import com.example.trade.demo.domain.entity.MarketDepthAggregator;
import com.example.trade.demo.domain.entity.OrderBookLevel;
import com.example.trade.demo.domain.fx.Adapters.MarketDepthAdapter;
import com.example.trade.demo.domain.fx.FxFlatCoordinator;
import com.example.trade.demo.domain.fx.FxTypes.ExecutionIntent;
import com.example.trade.demo.domain.fx.FxTypes.FxSymbol;
import com.example.trade.demo.domain.fx.FxTypes.FxSymbolRule;
import com.example.trade.demo.domain.fx.FxTypes.Side;
import com.example.trade.demo.domain.fx.FxTypes.TargetType;
import com.example.trade.demo.domain.fx.FlatModels.FlatOrder;
import com.example.trade.demo.domain.fx.FlatModels.FlatSignal;
import com.example.trade.demo.domain.fx.FlatModels.VwapParams;
import com.example.trade.demo.domain.fx.OmsAndRepo.InMemoryFlatRepo;
import com.example.trade.demo.domain.fx.OmsAndRepo.InMemoryOms;
import com.example.trade.demo.domain.fx.OmsAndRepo.LogRiskGateway;
import com.example.trade.demo.domain.fx.OmsAndRepo.RiskGateway;
import com.example.trade.demo.domain.fx.VwapStrategy.FxVwapStrategy;

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
		MarketDepthAdapter md = new MarketDepthAdapter(agg, symbol, rule);

		// 2) 准备执行环境
		InMemoryOms oms = new InMemoryOms();
		InMemoryFlatRepo repo = new InMemoryFlatRepo();
		RiskGateway risk = new LogRiskGateway();
		ScheduledExecutorService timer = FxFlatCoordinator.newTimer();
		FxFlatCoordinator coord = new FxFlatCoordinator(md, new FxVwapStrategy(), oms, repo, risk, timer);

		// 3) 触发一个 BUY Base 按 Base 数量目标的平盘
		FlatSignal sig = new FlatSignal(symbol, Side.BUY, new BigDecimal("3.5"), "rebalance", Instant.now());
		VwapParams p = new VwapParams(symbol, TargetType.BASE_QTY, new BigDecimal("3.5"),
				ExecutionIntent.AUTO, Duration.ofSeconds(2), new BigDecimal("0.00010"));
		String flatId = coord.start(sig, p);
		System.out.println("Started flatId=" + flatId);

		Thread.sleep(1000);
		// 4) 输出父单聚合结果
		FlatOrder fo = repo.load(flatId);
		System.out.println("Parent status=" + fo.status + ", cumBase=" + fo.cumBase + ", cumQuote=" + fo.cumQuote);
	}
}


