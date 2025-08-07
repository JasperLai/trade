package com.example.trade.demo.domain.entity;

// 执行结果 (用于反馈)
public class ExecutionResult {
    private final boolean success;
    private final String message;
    private final QuoteInstruction instruction;
    private final long executeTime;

    public ExecutionResult(boolean success, String message, QuoteInstruction instruction) {
        this.success = success;
        this.message = message;
        this.instruction = instruction;
        this.executeTime = System.currentTimeMillis();
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public QuoteInstruction getInstruction() { return instruction; }
    public long getExecuteTime() { return executeTime; }

    @Override
    public String toString() {
        return String.format("ExecutionResult{success=%s, msg='%s', time=%d}", success, message, executeTime);
    }
}
