# API 文档

## 概述

本文档描述了行情数据接收与报价系统的核心API接口。系统采用DDD架构设计，提供完整的报价功能，支持三种报价模式和风控检查。

## 核心API

### 1. QuoteInstruction API

#### 创建报价指令

**买价模式**
```java
public static QuoteInstruction createBidQuote(String symbol, BigDecimal bidPrice, BigDecimal size, String strategyName)
```

**参数**:
- `symbol` (String): 交易对标识，如 "BTCUSDT"
- `bidPrice` (BigDecimal): 买价
- `size` (BigDecimal): 数量
- `strategyName` (String): 策略名称

**返回值**: QuoteInstruction 对象

**示例**:
```java
QuoteInstruction bidQuote = QuoteInstruction.createBidQuote(
    "BTCUSDT", 
    new BigDecimal("100.0"), 
    new BigDecimal("1.0"), 
    "MyStrategy"
);
```

**卖价模式**
```java
public static QuoteInstruction createAskQuote(String symbol, BigDecimal askPrice, BigDecimal size, String strategyName)
```

**参数**:
- `symbol` (String): 交易对标识
- `askPrice` (BigDecimal): 卖价
- `size` (BigDecimal): 数量
- `strategyName` (String): 策略名称

**返回值**: QuoteInstruction 对象

**示例**:
```java
QuoteInstruction askQuote = QuoteInstruction.createAskQuote(
    "BTCUSDT", 
    new BigDecimal("101.0"), 
    new BigDecimal("1.0"), 
    "MyStrategy"
);
```

**买卖价模式**
```java
public static QuoteInstruction createBidAskQuote(String symbol, BigDecimal bidPrice, BigDecimal askPrice, BigDecimal size, String strategyName)
```

**参数**:
- `symbol` (String): 交易对标识
- `bidPrice` (BigDecimal): 买价
- `askPrice` (BigDecimal): 卖价
- `size` (BigDecimal): 数量
- `strategyName` (String): 策略名称

**返回值**: QuoteInstruction 对象

**示例**:
```java
QuoteInstruction bidAskQuote = QuoteInstruction.createBidAskQuote(
    "BTCUSDT", 
    new BigDecimal("100.0"), 
    new BigDecimal("101.0"), 
    new BigDecimal("1.0"), 
    "MyStrategy"
);
```

#### 查询报价指令属性

```java
// 获取交易对
String symbol = instruction.getSymbol();

// 获取报价类型
QuoteInstruction.QuoteType quoteType = instruction.getQuoteType();

// 获取买价
BigDecimal bidPrice = instruction.getBidPrice();

// 获取卖价
BigDecimal askPrice = instruction.getAskPrice();

// 获取数量
BigDecimal size = instruction.getSize();

// 获取策略名称
String strategyName = instruction.getStrategyName();

// 获取时间戳
long timestamp = instruction.getTimestamp();
```

#### 业务方法

```java
// 判断是否为买价模式
boolean isBidQuote = instruction.isBidQuote();

// 判断是否为卖价模式
boolean isAskQuote = instruction.isAskQuote();

// 判断是否为买卖价模式
boolean isBidAskQuote = instruction.isBidAskQuote();

// 获取主要报价价格
BigDecimal quotePrice = instruction.getQuotePrice();

// 获取方向（兼容性方法）
String side = instruction.getSide();
```

### 2. QuoteService API

#### 处理行情数据

```java
public void onMarketData(MarketDataEvent event)
```

**参数**:
- `event` (MarketDataEvent): 行情数据事件

**功能**:
- 接收行情数据
- 聚合深度数据
- 调用策略生成报价指令
- 执行风控检查
- 反馈执行结果

**示例**:
```java
// 创建行情数据
MarketDataEvent event = new MarketDataEvent(
    "BTCUSDT", 
    "ProviderA",
    Arrays.asList(
        new OrderBookLevel(new BigDecimal("100.5"), new BigDecimal("10")),
        new OrderBookLevel(new BigDecimal("100.6"), new BigDecimal("20"))
    ),
    Arrays.asList(
        new OrderBookLevel(new BigDecimal("100.4"), new BigDecimal("15")),
        new OrderBookLevel(new BigDecimal("100.3"), new BigDecimal("25"))
    )
);

// 处理行情数据
QuoteService service = new QuoteService(strategy);
service.onMarketData(event);
```

### 3. QuoteStrategy API

#### 策略接口

```java
public interface QuoteStrategy {
    QuoteInstruction decideQuote(MarketDepthAggregator aggregator);
}
```

**参数**:
- `aggregator` (MarketDepthAggregator): 市场深度聚合器

**返回值**: QuoteInstruction 对象或 null（如果无法生成指令）

#### 简单最优价格策略

```java
public class SimpleBestPriceStrategy implements QuoteStrategy {
    public SimpleBestPriceStrategy()
    public SimpleBestPriceStrategy(StrategyType strategyType)
}
```

**策略类型**:
```java
public enum StrategyType {
    BID_ONLY,      // 只报买价
    ASK_ONLY,      // 只报卖价
    BID_ASK        // 报买卖价
}
```

**示例**:
```java
// 买价策略
SimpleBestPriceStrategy bidStrategy = new SimpleBestPriceStrategy(
    SimpleBestPriceStrategy.StrategyType.BID_ONLY
);

// 卖价策略
SimpleBestPriceStrategy askStrategy = new SimpleBestPriceStrategy(
    SimpleBestPriceStrategy.StrategyType.ASK_ONLY
);

// 买卖价策略
SimpleBestPriceStrategy bidAskStrategy = new SimpleBestPriceStrategy(
    SimpleBestPriceStrategy.StrategyType.BID_ASK
);
```

### 4. QuoteExecutor API

#### 执行报价指令

```java
public ExecutionResult executeQuote(QuoteInstruction instruction)
```

**参数**:
- `instruction` (QuoteInstruction): 报价指令

**返回值**: ExecutionResult 对象

**功能**:
- 执行风控检查
- 执行报价指令
- 返回执行结果

**示例**:
```java
QuoteExecutor executor = new QuoteExecutor();
ExecutionResult result = executor.executeQuote(instruction);

if (result.isSuccess()) {
    System.out.println("执行成功: " + result.getMessage());
} else {
    System.out.println("执行失败: " + result.getMessage());
}
```

### 5. MarketDataEvent API

#### 创建行情数据事件

```java
public MarketDataEvent(String symbol, String provider, List<OrderBookLevel> askLevels, List<OrderBookLevel> bidLevels)
```

**参数**:
- `symbol` (String): 交易对标识
- `provider` (String): 数据提供商
- `askLevels` (List<OrderBookLevel>): 卖盘深度数据
- `bidLevels` (List<OrderBookLevel>): 买盘深度数据

**示例**:
```java
MarketDataEvent event = new MarketDataEvent(
    "BTCUSDT",
    "ProviderA",
    Arrays.asList(
        new OrderBookLevel(new BigDecimal("100.5"), new BigDecimal("10")),
        new OrderBookLevel(new BigDecimal("100.6"), new BigDecimal("20"))
    ),
    Arrays.asList(
        new OrderBookLevel(new BigDecimal("100.4"), new BigDecimal("15")),
        new OrderBookLevel(new BigDecimal("100.3"), new BigDecimal("25"))
    )
);
```

#### 查询行情数据属性

```java
// 获取交易对
String symbol = event.getSymbol();

// 获取数据提供商
String provider = event.getProvider();

// 获取卖盘深度
List<OrderBookLevel> askLevels = event.getAskLevels();

// 获取买盘深度
List<OrderBookLevel> bidLevels = event.getBidLevels();
```

### 6. OrderBookLevel API

#### 创建订单簿层级

```java
public OrderBookLevel(BigDecimal price, BigDecimal quantity)
```

**参数**:
- `price` (BigDecimal): 价格
- `quantity` (BigDecimal): 数量

**示例**:
```java
OrderBookLevel level = new OrderBookLevel(
    new BigDecimal("100.5"), 
    new BigDecimal("10")
);
```

#### 查询订单簿层级属性

```java
// 获取价格
BigDecimal price = level.getPrice();

// 获取数量
BigDecimal quantity = level.getQuantity();
```

### 7. MarketDepthAggregator API

#### 创建市场深度聚合器

```java
public MarketDepthAggregator(String symbol)
```

**参数**:
- `symbol` (String): 交易对标识

**示例**:
```java
MarketDepthAggregator aggregator = new MarketDepthAggregator("BTCUSDT");
```

#### 更新深度数据

```java
public void updateDepth(String provider, List<OrderBookLevel> askLevels, List<OrderBookLevel> bidLevels)
```

**参数**:
- `provider` (String): 数据提供商
- `askLevels` (List<OrderBookLevel>): 卖盘深度数据
- `bidLevels` (List<OrderBookLevel>): 买盘深度数据

**示例**:
```java
aggregator.updateDepth("ProviderA", askLevels, bidLevels);
```

#### 查询最优价格

```java
// 获取最优买价
Optional<BigDecimal> bestBid = aggregator.getBestBid();

// 获取最优卖价
Optional<BigDecimal> bestAsk = aggregator.getBestAsk();
```

**示例**:
```java
Optional<BigDecimal> bestBid = aggregator.getBestBid();
if (bestBid.isPresent()) {
    System.out.println("最优买价: " + bestBid.get());
}
```

### 8. ExecutionResult API

#### 创建执行结果

```java
public ExecutionResult(boolean success, String message, QuoteInstruction instruction)
```

**参数**:
- `success` (boolean): 执行是否成功
- `message` (String): 执行消息
- `instruction` (QuoteInstruction): 报价指令

**示例**:
```java
ExecutionResult result = new ExecutionResult(true, "执行成功", instruction);
```

#### 查询执行结果属性

```java
// 检查是否成功
boolean success = result.isSuccess();

// 获取执行消息
String message = result.getMessage();

// 获取报价指令
QuoteInstruction instruction = result.getInstruction();

// 获取执行时间
long executeTime = result.getExecuteTime();
```

## 错误处理

### 1. 风控检查失败

当报价指令不通过风控检查时，`QuoteExecutor.executeQuote()` 会返回失败的 `ExecutionResult`：

```java
ExecutionResult result = executor.executeQuote(instruction);
if (!result.isSuccess()) {
    System.out.println("风控检查失败: " + result.getMessage());
}
```

### 2. 策略无法生成指令

当策略无法基于当前市场数据生成报价指令时，会返回 null：

```java
QuoteInstruction instruction = strategy.decideQuote(aggregator);
if (instruction == null) {
    System.out.println("策略无法生成指令");
}
```

### 3. 数据验证失败

当输入数据无效时，会抛出异常或返回错误结果：

```java
try {
    // 处理行情数据
    service.onMarketData(event);
} catch (Exception e) {
    System.err.println("数据处理失败: " + e.getMessage());
}
```

## 使用示例

### 完整的工作流程

```java
// 1. 创建策略
SimpleBestPriceStrategy strategy = new SimpleBestPriceStrategy(
    SimpleBestPriceStrategy.StrategyType.BID_ASK
);

// 2. 创建服务
QuoteService service = new QuoteService(strategy);

// 3. 创建行情数据
MarketDataEvent event = new MarketDataEvent(
    "BTCUSDT", 
    "ProviderA",
    Arrays.asList(
        new OrderBookLevel(new BigDecimal("100.5"), new BigDecimal("10")),
        new OrderBookLevel(new BigDecimal("100.6"), new BigDecimal("20"))
    ),
    Arrays.asList(
        new OrderBookLevel(new BigDecimal("100.4"), new BigDecimal("15")),
        new OrderBookLevel(new BigDecimal("100.3"), new BigDecimal("25"))
    )
);

// 4. 处理行情数据
service.onMarketData(event);
```

### 手动创建报价指令

```java
// 创建买价指令
QuoteInstruction bidInstruction = QuoteInstruction.createBidQuote(
    "BTCUSDT", 
    new BigDecimal("100.0"), 
    new BigDecimal("1.0"), 
    "ManualStrategy"
);

// 创建卖价指令
QuoteInstruction askInstruction = QuoteInstruction.createAskQuote(
    "BTCUSDT", 
    new BigDecimal("101.0"), 
    new BigDecimal("1.0"), 
    "ManualStrategy"
);

// 创建买卖价指令
QuoteInstruction bidAskInstruction = QuoteInstruction.createBidAskQuote(
    "BTCUSDT", 
    new BigDecimal("100.0"), 
    new BigDecimal("101.0"), 
    new BigDecimal("1.0"), 
    "ManualStrategy"
);
```

### 执行报价指令

```java
// 创建执行器
QuoteExecutor executor = new QuoteExecutor();

// 执行买价指令
ExecutionResult bidResult = executor.executeQuote(bidInstruction);
System.out.println("买价执行结果: " + bidResult.isSuccess());

// 执行卖价指令
ExecutionResult askResult = executor.executeQuote(askInstruction);
System.out.println("卖价执行结果: " + askResult.isSuccess());

// 执行买卖价指令
ExecutionResult bidAskResult = executor.executeQuote(bidAskInstruction);
System.out.println("买卖价执行结果: " + bidAskResult.isSuccess());
```

## 性能考虑

### 1. 对象创建

- 使用静态工厂方法创建 `QuoteInstruction` 对象
- 重用 `MarketDepthAggregator` 实例
- 避免频繁创建临时对象

### 2. 内存管理

- 使用不可变对象减少内存开销
- 及时释放不再使用的对象引用
- 使用适当的数据结构（如 `TreeMap` 保证有序性）

### 3. 并发处理

- 使用线程安全的数据结构（如 `ConcurrentHashMap`）
- 避免在关键路径中使用锁
- 使用原子操作进行状态更新

## 最佳实践

### 1. 错误处理

- 始终检查 `ExecutionResult.isSuccess()`
- 处理策略返回 null 的情况
- 使用 try-catch 处理异常

### 2. 性能优化

- 重用对象实例
- 使用适当的数据结构
- 避免不必要的对象创建

### 3. 代码可读性

- 使用有意义的变量名
- 添加适当的注释
- 遵循命名约定

### 4. 测试

- 为每个API编写单元测试
- 测试边界条件和异常情况
- 使用模拟对象进行集成测试

## 版本兼容性

### 向后兼容性

- 保持现有API接口不变
- 新增功能通过扩展方法实现
- 使用默认参数保持兼容性

### 升级指南

- 检查API变更
- 更新依赖版本
- 运行测试验证功能

---

**注意**: 本文档描述了当前版本的API。在升级版本时，请参考相应的变更日志。
