# 阶段 1: 基础架构与插件系统 - 上下文

**收集时间:** 2026 年 3 月 24 日
**状态:** 准备规划

<domain>
## 阶段边界

**本阶段交付**: 建立微内核 + 插件化基础架构，实现插件生命周期管理和基础存储能力

**包含**:
- 插件接口定义和 SPI 机制
- 核心引擎（插件加载器、生命周期管理、事件总线）
- 会话状态机实现
- 基础存储插件（本地磁盘）
- 元数据持久化到 PostgreSQL

**不包含**:
- 实际的音视频采集（阶段 2）
- 加密归档功能（阶段 3）
- AI 能力集成（阶段 4）
- 流程编排引擎（阶段 5）
- 管理后台界面（阶段 8）

</domain>

<decisions>
## 实施决策

### 插件接口设计

**接口机制**: Java SPI
- 使用 `ServiceLoader` 发现和加载插件
- 插件实现 `Plugin` 接口：`init()`, `execute()`, `destroy()`, `getMetadata()`
- 通过 `META-INF/services/` 配置文件声明插件实现

**参数和返回值**: PluginContext + PluginResult 对象
- `PluginContext` 包含所有输入参数和配置
- `PluginResult` 包含执行结果、状态码、错误信息
- 类型安全，易于扩展和测试

**配置管理**: 配置文件 + 注解注入
- 每个插件有独立 YAML 配置文件
- 使用 `@Config` 注解将配置注入到插件字段
- 配置文件位于 `plugins/{plugin-name}/config.yaml`

**错误处理**: PluginException + 错误码
- 定义 `PluginException` 基类和子类（`PluginInitException`, `PluginExecutionException`, `PluginDestroyException`）
- 错误码枚举定义（如：`ERR_PLUGIN_INIT_FAILED`, `ERR_PLUGIN_EXECUTION_TIMEOUT`）
- 所有插件异常统一捕获并记录到审计日志

### 核心引擎架构

**内核职责**: 最小化内核
- 核心引擎只负责：插件加载/卸载、生命周期调度、插件总线、状态机管理
- 所有业务逻辑由插件实现
- 核心引擎不包含任何业务特定代码

**状态机**: 状态模式 + 枚举
- 使用 Java 枚举定义状态：`IDLE`, `INITIALIZING`, `RECORDING`, `INSPECTING`, `COMPLETED`, `ERROR`
- 每个状态是一个类实现 `State` 接口
- 状态转换由事件触发（`StartRecording`, `StopRecording`, `StartInspection`, `Complete`, `Error`）

**插件总线**: 事件总线模式
- 使用事件驱动架构（Event Bus）
- 插件订阅感兴趣的事件类型
- 核心引擎发布事件到总线，插件异步处理
- 支持同步和异步事件处理

### 数据库与存储设计

**数据库**: PostgreSQL
- 版本：PostgreSQL 15+
- 使用连接池：HikariCP
- 支持 JSONB 字段用于灵活扩展

**ORM 框架**: MyBatis + XML 映射
- 使用 MyBatis 3.x
- SQL 映射文件位于 `src/main/resources/mappers/`
- 支持动态 SQL 和结果映射

**表结构**: 按插件域分组
- 表名格式：`{domain}_xxx`
- 示例：
  - `flow_session` (双录会话表)
  - `flow_state_history` (状态流转历史表)
  - `storage_file` (文件存储元数据表)
  - `plugin_instance` (插件实例表)

**基础存储插件**:
- 支持本地磁盘存储
- 文件路径配置化
- 元数据（文件路径、大小、哈希值、创建时间）存储到数据库

### 项目结构组织

**构建工具**: Maven 多模块
- 父 POM：`aidrclaw-parent` (管理依赖版本、插件配置)
- 子模块划分：
  - `aidrclaw-core`: 核心引擎
  - `aidrclaw-plugin-api`: 插件 API 定义
  - `aidrclaw-plugins-storage`: 存储插件实现
  - `aidrclaw-plugins-flow`: 流程插件实现

**插件打包**: 独立 JAR + MANIFEST
- 每个插件打包为独立 JAR 文件
- `MANIFEST.MF` 声明插件元数据：
  - `Plugin-Class`: 插件实现类
  - `Plugin-Id`: 插件唯一标识
  - `Plugin-Version`: 插件版本 (semver)
  - `Plugin-API-Version`: 兼容的插件 API 版本
- 部署时放入 `plugins/` 目录

**版本管理**: 语义化版本
- 遵循 semver (主版本。次版本。补丁版本)
- 主版本变更：不兼容的 API 变更
- 次版本变更：向后兼容的功能新增
- 补丁版本变更：向后兼容的问题修复
- 核心引擎检查插件 API 兼容性

</decisions>

<code_context>
## 现有代码洞察

### 可复用资产

**无** - 这是 greenfield 项目，没有现有代码可复用

### 既定模式

**无** - 需要在阶段规划中建立初始项目结构和编码规范

### 集成点

- 数据库连接：PostgreSQL JDBC 驱动
- 事件总线：Guava EventBus 或 Spring Event
- MyBatis: XML 映射配置
- Maven: 多模块父 POM 配置

</code_context>

<specifics>
## 特定要求

**技术栈偏好**:
- Java 17+ (金融企业常用 LTS 版本)
- Spring Boot 3.x (可选，用于基础服务如日志、配置)
- PostgreSQL 15+
- MyBatis 3.x
- Maven 3.8+

**架构原则**:
- 微内核 + 插件化：核心引擎尽可能小，业务逻辑由插件实现
- 事件驱动：插件间通过事件总线解耦通信
- 配置外部化：所有配置通过配置文件或环境变量注入
- 错误可追踪：所有异常有明确错误码和审计日志

</specifics>

<deferred>
## 延期想法

**无** - 讨论严格保持在阶段 1 范围内

</deferred>

---

*阶段：01-basic-architecture*
*上下文收集：2026 年 3 月 24 日*
