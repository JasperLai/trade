package com.example.trade.demo.domain.service;

import org.springframework.stereotype.Service;

import com.example.trade.demo.domain.entity.ExecutionResult;

@Service
public class LogAndFeedBack {

    // Log 模块负责接收 ExecutionResult 并进行处理
    public void handleExecutionResult(ExecutionResult result) {
        if (result.isSuccess()) {
            System.out.println(String.format("[日志&反馈] ✅ %s | 指令: %s", result.getMessage(), result.getInstruction()));
        } else {
            System.out.println(String.format("[日志&反馈] ❌ %s | 指令: %s", result.getMessage(), result.getInstruction()));
            sendAlert(result);
        }
        // 模拟向市场/上游系统发送反馈
        sendFeedbackToMarket(result);
    }

    private void sendAlert(ExecutionResult result) {
        System.err.println(String.format("[日志&反馈] ⚠️ 告警: %s (Symbol: %s)",
                                       result.getMessage(), result.getInstruction().getSymbol()));
    }

    private void sendFeedbackToMarket(ExecutionResult result) {
        // 这里可以是发送消息到消息队列、调用回调接口、更新状态等
        System.out.println(String.format("[日志&反馈] ↩️ 向市场发送反馈结果: %s", result.getInstruction().getSymbol()));
    }
}
