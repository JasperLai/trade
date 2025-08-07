# 行情数据接收与报价系统

## 项目概述

这是一个基于DDD（领域驱动设计）的行情数据接收和自动报价系统，采用Spring Boot 3.5.4框架开发。系统能够接收多源行情数据，聚合深度信息，并根据策略生成不同类型的报价指令。

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.5.4
- **Maven**: 3.x
- **MySQL**: 8.0 (配置了连接器)
- **架构模式**: DDD (领域驱动设计)

## 项目结构

```
demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/trade/demo/
│   │   │   ├── domain/
│   │   │   │   ├── entity/           # 领域实体
│   │   │   │   │   ├── ExecutionResult.java
│   │   │   │   │   ├── MarketDataEvent.java
│   │   │   │   │   ├── MarketDepthAggregator.java
│   │   │   │   │   ├── OrderBookLevel.java
│   │   │   │   │   └── QuoteInstruction.java
│   │   │   │   └── service/          # 领域服务
│   │   │   │       ├── LogAndFeedBack.java
│   │   │   │       ├── QuoteExecutor.java
│   │   │   │       ├── QuoteService.java
│   │   │   │       ├── QuoteStrategy.java
│   │   │   │       └── SimpleBestPriceStrategy.java
│   │   │   ├── DemoApplication.java
│   │   │   ├── QuoteSystemDemo.java
│   │   │   └── QuoteModeDemo.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/example/trade/demo/
│           ├── DemoApplicationTests.java
│           ├── QuoteExecutorTest.java
│           └── QuoteInstructionTest.java
├── pom.xml
└── README.md
```

## 核心功能

### 1. 三种报价模式

系统支持三种不同的报价模式：

#### 买价模式 (BID)
- 只包含买价，适用于只想买入的场景
- 风控检查：买价不高于上限150.0

#### 卖价模式 (ASK)
- 只包含卖价，适用于只想卖出的场景  
- 风控检查：卖价不低于下限30.0

#### 买卖价模式 (BID_ASK)
- 同时包含买价和卖价，适用于做市商场景
- 风控检查：买价不高于上限150.0，卖价不低于下限30.0，且买价必须小于卖价

### 2. 行情数据聚合

- 支持多数据源行情数据接收
- 自动聚合不同提供商的深度数据
- 维护最优买卖价格

### 3. 策略驱动报价

- 基于聚合的深度数据生成报价指令
- 支持多种报价策略
- 可扩展的策略接口设计

### 4. 风控检查

- 针对不同报价模式的专门风控逻辑
- 价格范围检查
- 买卖价差验证
- 实时风控反馈

## DDD架构设计

### 领域层 (Domain Layer)

#### 实体 (Entities)

**MarketDataEvent**
```java
// 行情数据事件
public class MarketDataEvent {
    private final String symbol;        // 交易对
    private final String provider;      // 数据提供商
    private final List<OrderBookLevel> askLevels;  // 卖盘深度
    private final List<OrderBookLevel> bidLevels;  // 买盘深度
}
```

**QuoteInstruction**
```java
// 报价指令 - 支持三种报价模式
public class QuoteInstruction {
    public enum QuoteType {
        BID,        // 买价模式
        ASK,        // 卖价模式  
        BID_ASK     // 买卖价模式
    }
    
    // 静态工厂方法
    public static QuoteInstruction createBidQuote(...)
    public static QuoteInstruction createAskQuote(...)
    public static QuoteInstruction createBidAskQuote(...)
}
```

**MarketDepthAggregator**
```java
// 市场深度聚合器
public class MarketDepthAggregator {
    // 按Symbol管理，存储完整深度
    // 聚合多个数据源的深度数据
    // 提供最优买卖价格查询
}
```

#### 领域服务 (Domain Services)

**QuoteStrategy** - 报价策略接口
```java
public interface QuoteStrategy {
    QuoteInstruction decideQuote(MarketDepthAggregator aggregator);
}
```

**QuoteService** - 核心报价服务
```java
@Service
public class QuoteService {
    // 协调整个报价流程
    // 1. 接收行情数据
    // 2. 聚合深度数据
    // 3. 调用策略生成指令
    // 4. 执行风控检查
    // 5. 反馈执行结果
}
```

**QuoteExecutor** - 报价执行器
```java
public class QuoteExecutor {
    // 执行报价指令
    // 进行风控检查
    // 返回执行结果
}
```

### 业务流程

```
行情数据接收 → 深度聚合 → 策略决策 → 风控检查 → 指令执行 → 结果反馈
```

## 使用指南

### 环境要求

1. **Java 17**
```bash
java -version
# 确保版本为 17.x
```

2. **Maven 3.x**
```bash
mvn -version
```

### 编译项目

```bash
# 设置Java 17环境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home

# 编译项目
mvn compile
```

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=QuoteInstructionTest
mvn test -Dtest=QuoteExecutorTest
```

### 运行演示程序

#### 1. 基础功能演示
```bash
mvn spring-boot:run -Dspring-boot.run.main-class=com.example.trade.demo.QuoteSystemDemo
```

#### 2. 三种报价模式演示
```bash
mvn spring-boot:run -Dspring-boot.run.main-class=com.example.trade.demo.QuoteModeDemo
```

## API 使用示例

### 创建报价指令

```java
// 买价模式
QuoteInstruction bidQuote = QuoteInstruction.createBidQuote(
    "BTCUSDT", new BigDecimal("100.0"), new BigDecimal("1.0"), "MyStrategy"
);

// 卖价模式
QuoteInstruction askQuote = QuoteInstruction.createAskQuote(
    "BTCUSDT", new BigDecimal("101.0"), new BigDecimal("1.0"), "MyStrategy"
);

// 买卖价模式
QuoteInstruction bidAskQuote = QuoteInstruction.createBidAskQuote(
    "BTCUSDT", new BigDecimal("100.0"), new BigDecimal("101.0"), 
    new BigDecimal("1.0"), "MyStrategy"
);
```

### 使用不同策略

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

### 处理行情数据

```java
// 创建行情数据
MarketDataEvent event = new MarketDataEvent(
    "BTCUSDT", "ProviderA",
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

## 风控规则

### 买价模式风控
- 买价不能为空
- 买价必须大于0
- 买价不高于上限150.0

### 卖价模式风控
- 卖价不能为空
- 卖价必须大于0
- 卖价不低于下限30.0

### 买卖价模式风控
- 买价和卖价都不能为空
- 价格必须大于0
- 买价必须小于卖价
- 买价不高于上限150.0
- 卖价不低于下限30.0

## 扩展指南

### 添加新的报价策略

1. 实现 `QuoteStrategy` 接口
```java
public class MyCustomStrategy implements QuoteStrategy {
    @Override
    public QuoteInstruction decideQuote(MarketDepthAggregator aggregator) {
        // 实现自定义策略逻辑
        return QuoteInstruction.createBidAskQuote(...);
    }
}
```

2. 在服务中使用新策略
```java
QuoteService service = new QuoteService(new MyCustomStrategy());
```

### 修改风控规则

在 `QuoteExecutor` 类中修改相应的风控方法：
- `checkBidQuote()` - 买价模式风控
- `checkAskQuote()` - 卖价模式风控  
- `checkBidAskQuote()` - 买卖价模式风控

### 添加新的报价模式

1. 在 `QuoteInstruction.QuoteType` 枚举中添加新类型
2. 添加相应的构造函数和静态工厂方法
3. 在 `QuoteExecutor` 中添加对应的风控检查方法

## 测试覆盖

### 单元测试

- `QuoteInstructionTest` - 测试三种报价模式的创建和属性
- `QuoteExecutorTest` - 测试风控逻辑和指令执行

### 集成测试

- `QuoteSystemDemo` - 基础功能集成测试
- `QuoteModeDemo` - 三种报价模式集成测试

## 性能考虑

1. **并发处理**: 使用 `ConcurrentHashMap` 保证线程安全
2. **内存管理**: 使用不可变对象减少内存开销
3. **延迟优化**: 异步处理行情数据和报价执行

## 监控和日志

系统提供详细的日志输出：
- 行情数据接收日志
- 策略决策日志
- 风控检查日志
- 执行结果日志

## 故障排除

### 常见问题

1. **Java版本不匹配**
   ```bash
   # 确保使用Java 17
   export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
   ```

2. **Bean定义冲突**
   - 检查是否有重复的Bean名称
   - 使用不同的Bean名称

3. **风控检查失败**
   - 检查价格是否在允许范围内
   - 确认买卖价差是否合理

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 Issue
- 发送邮件
- 参与讨论

---

**注意**: 这是一个演示项目，生产环境使用前请进行充分测试和配置调整。
