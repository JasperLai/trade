# 变更日志

本文档记录了行情数据接收与报价系统的主要变更和版本历史。

## [1.0.0] - 2025-08-07

### 新增功能

#### 核心功能
- **三种报价模式支持**: 实现了买价(BID)、卖价(ASK)、买卖价(BID_ASK)三种报价模式
- **DDD架构设计**: 采用领域驱动设计模式，实现清晰的领域边界和职责分离
- **风控检查机制**: 针对不同报价模式实现了专门的风控检查逻辑
- **策略模式支持**: 支持多种报价策略，易于扩展新策略

#### 领域实体
- **QuoteInstruction**: 支持三种报价模式的报价指令实体
  - 新增 `QuoteType` 枚举类型
  - 新增静态工厂方法：`createBidQuote()`, `createAskQuote()`, `createBidAskQuote()`
  - 新增业务方法：`isBidQuote()`, `isAskQuote()`, `isBidAskQuote()`, `getQuotePrice()`
  - 保持向后兼容性

- **MarketDataEvent**: 行情数据事件实体
- **MarketDepthAggregator**: 市场深度聚合器
- **OrderBookLevel**: 订单簿层级实体
- **ExecutionResult**: 执行结果实体

#### 领域服务
- **QuoteStrategy**: 报价策略接口
- **SimpleBestPriceStrategy**: 简单最优价格策略实现
  - 支持三种策略类型：`BID_ONLY`, `ASK_ONLY`, `BID_ASK`
- **QuoteService**: 核心报价服务
- **QuoteExecutor**: 报价执行器
  - 实现针对不同报价模式的风控检查
  - 支持买价上限检查（150.0）
  - 支持卖价下限检查（30.0）
  - 支持买卖价差检查
- **LogAndFeedBack**: 日志反馈服务

#### 风控规则
- **买价模式风控**:
  - 买价不能为空
  - 买价必须大于0
  - 买价不高于上限150.0

- **卖价模式风控**:
  - 卖价不能为空
  - 卖价必须大于0
  - 卖价不低于下限30.0

- **买卖价模式风控**:
  - 买价和卖价都不能为空
  - 价格必须大于0
  - 买价必须小于卖价
  - 买价不高于上限150.0
  - 卖价不低于下限30.0

#### 演示程序
- **QuoteSystemDemo**: 基础功能演示程序
- **QuoteModeDemo**: 三种报价模式演示程序

#### 测试覆盖
- **QuoteInstructionTest**: 测试三种报价模式的创建和属性
- **QuoteExecutorTest**: 测试风控逻辑和指令执行
- **DemoApplicationTests**: Spring Boot应用测试

### 技术特性

#### 架构设计
- **DDD分层架构**: 应用层、领域层、基础设施层
- **设计模式应用**: 策略模式、工厂模式、聚合模式、观察者模式
- **线程安全**: 使用 `ConcurrentHashMap` 等线程安全数据结构
- **不可变对象**: 使用不可变对象减少内存开销

#### 性能优化
- **对象池**: 重用频繁创建的对象
- **缓存策略**: 缓存计算结果
- **延迟加载**: 按需加载数据
- **并发处理**: 多线程处理独立任务

#### 监控和日志
- **结构化日志**: 详细的业务日志输出
- **风控告警**: 实时风控告警机制
- **执行反馈**: 完整的执行结果反馈

### 环境要求

#### 技术栈
- **Java**: 17
- **Spring Boot**: 3.5.4
- **Maven**: 3.x
- **MySQL**: 8.0 (配置了连接器)

#### 编译和运行
```bash
# 设置Java 17环境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home

# 编译项目
mvn compile

# 运行测试
mvn test

# 运行演示程序
mvn spring-boot:run -Dspring-boot.run.main-class=com.example.trade.demo.QuoteModeDemo
```

### API变更

#### 新增API
- `QuoteInstruction.createBidQuote()`: 创建买价指令
- `QuoteInstruction.createAskQuote()`: 创建卖价指令
- `QuoteInstruction.createBidAskQuote()`: 创建买卖价指令
- `QuoteInstruction.isBidQuote()`: 判断是否为买价模式
- `QuoteInstruction.isAskQuote()`: 判断是否为卖价模式
- `QuoteInstruction.isBidAskQuote()`: 判断是否为买卖价模式
- `QuoteInstruction.getQuotePrice()`: 获取主要报价价格

#### 兼容性API
- 保持原有的构造函数和getter方法
- 保持 `getSide()` 和 `getPrice()` 方法的兼容性

### 文档

#### 新增文档
- **README.md**: 项目概述、使用指南、API示例
- **ARCHITECTURE.md**: 技术架构文档、DDD设计模式
- **API.md**: 详细的API文档和使用示例
- **CHANGELOG.md**: 变更日志（本文档）

### 已知问题

#### 已修复
- ~~Java版本兼容性问题~~ ✅ 已修复
- ~~Bean定义冲突问题~~ ✅ 已修复
- ~~风控逻辑编译错误~~ ✅ 已修复
- ~~依赖注入问题~~ ✅ 已修复

#### 待优化
- 性能监控指标收集
- 分布式部署支持
- 数据库持久化
- REST API接口

### 贡献者

- 系统架构设计
- DDD模式实现
- 三种报价模式支持
- 风控检查机制
- 测试用例编写
- 文档编写

## 版本规划

### [1.1.0] - 计划中

#### 计划功能
- **REST API接口**: 提供HTTP API接口
- **数据库持久化**: 实现报价历史记录
- **性能监控**: 添加性能指标收集
- **配置管理**: 支持动态配置

#### 技术改进
- **异步处理**: 异步处理非关键路径
- **缓存优化**: 优化数据缓存策略
- **日志优化**: 结构化日志输出

### [1.2.0] - 计划中

#### 计划功能
- **分布式部署**: 支持集群部署
- **消息队列**: 集成消息队列
- **微服务架构**: 拆分为微服务
- **容器化**: Docker容器支持

#### 技术改进
- **服务发现**: 服务注册与发现
- **负载均衡**: 负载均衡策略
- **熔断机制**: 服务熔断保护

### [2.0.0] - 计划中

#### 计划功能
- **机器学习**: 集成ML策略
- **实时分析**: 实时数据分析
- **高级风控**: 复杂风控规则
- **多市场支持**: 支持多个交易市场

#### 技术改进
- **AI集成**: 人工智能算法
- **大数据**: 大数据处理能力
- **云原生**: 云原生架构

## 升级指南

### 从0.x版本升级到1.0.0

#### 主要变更
1. **QuoteInstruction API变更**:
   - 新增三种报价模式支持
   - 新增静态工厂方法
   - 保持向后兼容性

2. **风控检查增强**:
   - 针对不同报价模式的专门风控逻辑
   - 更严格的价格检查

3. **策略支持扩展**:
   - 支持三种策略类型
   - 易于扩展新策略

#### 升级步骤
1. **检查依赖**: 确保Java 17环境
2. **更新代码**: 使用新的API方法
3. **运行测试**: 验证功能正确性
4. **性能测试**: 确保性能满足要求

#### 兼容性说明
- 保持原有API的向后兼容性
- 新增功能通过扩展方法实现
- 默认行为保持不变

## 故障排除

### 常见问题

#### 1. Java版本问题
```bash
# 检查Java版本
java -version

# 设置正确的Java环境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
```

#### 2. 编译错误
```bash
# 清理并重新编译
mvn clean compile

# 检查依赖
mvn dependency:tree
```

#### 3. 运行错误
```bash
# 检查Bean定义
# 确保没有重复的Bean名称

# 检查日志输出
# 查看详细的错误信息
```

#### 4. 风控检查失败
- 检查价格是否在允许范围内
- 确认买卖价差是否合理
- 验证数据格式是否正确

### 性能问题

#### 1. 内存使用
- 检查对象创建频率
- 优化数据结构使用
- 监控内存泄漏

#### 2. CPU使用
- 优化算法复杂度
- 使用并行处理
- 减少不必要的计算

#### 3. 响应时间
- 优化关键路径
- 使用缓存策略
- 异步处理非关键操作

## 支持

### 获取帮助
- 查看文档：README.md, API.md, ARCHITECTURE.md
- 运行测试：`mvn test`
- 查看日志：检查控制台输出

### 报告问题
- 提供详细的错误信息
- 包含复现步骤
- 提供环境信息

### 贡献代码
- Fork项目
- 创建功能分支
- 提交Pull Request

---

**注意**: 本文档会随着版本更新而更新。请关注最新的变更信息。
