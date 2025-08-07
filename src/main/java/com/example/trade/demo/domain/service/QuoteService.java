package com.example.trade.demo.domain.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.trade.demo.domain.entity.ExecutionResult;
import com.example.trade.demo.domain.entity.MarketDataEvent;
import com.example.trade.demo.domain.entity.MarketDepthAggregator;
import com.example.trade.demo.domain.entity.QuoteInstruction;

@Service
public class QuoteService {
    private final Map<String, MarketDepthAggregator> aggregators = new ConcurrentHashMap<>();
    private final QuoteStrategy strategy;
    private final QuoteExecutor executor;

    @Autowired
    private LogAndFeedBack logAndFeedback; // 引入日志反馈模块

    public QuoteService(QuoteStrategy strategy) {
        this.strategy = strategy;
        this.executor = new QuoteExecutor(); // 初始化执行器
    }

    // 核心业务流程入口
    public void onMarketData(MarketDataEvent event) {
        System.out.println("\n--- [QuoteService] 处理行情事件 ---");
        System.out.println(String.format("[Market] 收到行情: %s", event));

        String symbol = event.getSymbol();

        // 1. 获取或创建聚合器实例
        MarketDepthAggregator aggregator = aggregators.computeIfAbsent(symbol, MarketDepthAggregator::new);
        System.out.println(String.format("[QuoteService] 使用聚合器: %s", aggregator.getSymbol()));

        // 2. 更新聚合器深度数据
        aggregator.updateDepth(event.getProvider(), event.getAskLevels(), event.getBidLevels());

        // 3. 调用策略生成指令
        QuoteInstruction instruction = strategy.decideQuote(aggregator);

        // 4. 如果有指令，则执行
        if (instruction != null) {
            // 5. 调用执行器执行指令，并获取执行结果
            ExecutionResult result = executor.executeQuote(instruction);

            // 6. 将执行结果传递给日志/反馈模块 (符合 sequenceDiagram)
            if (logAndFeedback != null) {
                logAndFeedback.handleExecutionResult(result);
            } else {
                // 如果依赖注入失败，使用简单的日志输出
                System.out.println(String.format("[QuoteService] 执行结果: %s", result));
            }
        } else {
            System.out.println(String.format("[QuoteService] %s: 策略未生成指令", symbol));
        }

        System.out.println("--- [QuoteService] 行情事件处理完毕 ---\n");
    }
}
