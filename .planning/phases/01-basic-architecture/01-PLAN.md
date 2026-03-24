# 阶段 1: 基础架构与插件系统 - 实施计划

**阶段目标**: 建立微内核 + 插件化基础架构，实现插件生命周期管理和基础存储能力

**需求映射**: FLOW-02, STORAGE-01, STORAGE-03

**成功标准**:
1. 插件接口定义完成（Plugin.init(), execute(), destroy()）
2. 插件加载器可发现和加载插件
3. 会话状态机可正确流转状态
4. 元数据可持久化到数据库
5. 基础存储插件可保存文件到本地磁盘

---

## 任务列表

### 任务 1: 创建 Maven 多模块项目结构

**目标**: 建立父 POM 和子模块结构，基于 Spring Boot 3.x

**子任务**:
1.1 创建父 POM (`aidrclaw-parent`)
   - 定义 Java 17、Maven 插件版本
   - **使用 Spring Boot 3.x 作为父依赖** (`spring-boot-starter-parent`)
   - 管理依赖版本 (MyBatis 3.x, PostgreSQL 驱动，HikariCP)
   - 配置 Maven 插件 (compiler, surefire, jar)

1.2 创建子模块
   - `aidrclaw-plugin-api`: 插件 API 接口定义
   - `aidrclaw-core`: 核心引擎实现 (**使用 Spring Boot Starter**)
   - `aidrclaw-plugins-storage`: 存储插件实现
   - `aidrclaw-plugins-flow`: 流程插件实现

**交付物**: 
- 父 POM 文件 (`pom.xml`) — **继承自 spring-boot-starter-parent**
- 4 个子模块目录结构和 `pom.xml`
- `.gitignore` 文件
- Spring Boot 主应用类 (`AidrclawApplication.java`)

**验收标准**:
- [x] `mvn clean install` 成功执行
- [x] 所有子模块正确继承父 POM
- [x] Spring Boot 应用可启动 (`mvn spring-boot:run`)
- [x] 依赖版本统一管理

**状态**: ✅ 已完成 (2026-03-24)

---

### 任务 2: 定义插件 API 接口

**目标**: 定义统一的插件接口和 SPI 机制

**子任务**:
2.1 定义 `Plugin` 接口
   ```java
   public interface Plugin {
       void init(PluginContext context);
       PluginResult execute(PluginContext context);
       void destroy();
       PluginMetadata getMetadata();
   }
   ```

2.2 定义 `PluginContext` 类
   - 包含配置 Map
   - 包含输入参数
   - 提供类型安全的 getter 方法

2.3 定义 `PluginResult` 类
   - 包含执行状态码
   - 包含结果数据 Map
   - 包含错误信息（可选）
   - 提供成功/失败工厂方法

2.4 定义 `PluginMetadata` 接口
   - `getPluginId()`: 插件唯一标识
   - `getVersion()`: 插件版本
   - `getName()`: 插件名称
   - `getDescription()`: 插件描述

2.5 定义 SPI 配置文件格式
   - `META-INF/services/com.aidrclaw.plugin.Plugin`
   - 每行一个插件实现类全限定名

**交付物**:
- `aidrclaw-plugin-api/src/main/java/com/aidrclaw/plugin/` 目录
- `Plugin.java`, `PluginContext.java`, `PluginResult.java`, `PluginMetadata.java`
- SPI 配置示例文件

**验收标准**:
- [x] 插件接口编译通过
- [x] 提供 SPI 配置示例
- [x] Javadoc 完整

**状态**: ✅ 已完成 (2026-03-24)

---

### 任务 3: 实现核心引擎

**目标**: 实现插件加载器、生命周期管理和事件总线，基于 Spring Boot

**子任务**:
3.1 实现 `PluginLoader` 类
   - 使用 `ServiceLoader` 发现插件
   - 管理插件实例生命周期
   - 提供 `loadPlugin()`, `unloadPlugin()` 方法
   - **使用 Spring Bean 管理插件实例**

3.2 实现 `PluginManager` 类
   - 维护已加载插件注册表
   - 提供插件查询方法 (`getPlugin()`, `listPlugins()`)
   - 处理插件依赖关系
   - **标记为 `@Service` 由 Spring 管理**

3.3 实现事件总线
   - **使用 Spring ApplicationEventPublisher**
   - 定义领域事件类 (继承 `ApplicationEvent`)
   - 使用 `@EventListener` 监听事件
   - 提供同步/异步事件处理

3.4 实现插件配置管理
   - 读取 YAML 配置文件
   - **使用 `@ConfigurationProperties` 注入配置**
   - **使用 `@RefreshScope` 支持配置热刷新 (可选)**

**交付物**:
- `aidrclaw-core/src/main/java/com/aidrclaw/core/plugin/` 目录
- `PluginLoader.java`, `PluginManager.java`
- **Spring 配置类 `PluginAutoConfiguration.java`**
- 事件定义类

**验收标准**:
- [ ] 可发现并加载测试插件
- [ ] 插件生命周期方法正确调用
- [ ] Spring 事件发布/订阅正常工作
- [ ] 配置通过 `@ConfigurationProperties` 正确注入
- [ ] Spring Boot 应用上下文正确加载

---

### 任务 4: 实现会话状态机

**目标**: 使用状态模式实现双录会话状态机

**子任务**:
4.1 定义会话状态枚举
   ```java
   public enum SessionState {
       IDLE, INITIALIZING, RECORDING, INSPECTING, COMPLETED, ERROR
   }
   ```

4.2 定义 `State` 接口
   ```java
   public interface State {
       void onEnter(SessionContext context);
       void onExit(SessionContext context);
       State onEvent(Event event, SessionContext context);
   }
   ```

4.3 实现具体状态类
   - `IdleState`: 空闲状态
   - `InitializingState`: 初始化状态
   - `RecordingState`: 录制中状态
   - `InspectingState`: 质检中状态
   - `CompletedState`: 完成状态
   - `ErrorState`: 错误状态

4.4 定义事件类
   - `StartRecordingEvent`
   - `StopRecordingEvent`
   - `StartInspectionEvent`
   - `CompleteEvent`
   - `ErrorEvent`

4.5 实现 `SessionStateMachine` 类
   - 维护当前状态
   - 处理状态转换
   - 记录状态历史

**交付物**:
- `aidrclaw-core/src/main/java/com/aidrclaw/core/session/` 目录
- `SessionState.java`, `State.java`, 6 个状态实现类
- 事件类定义
- `SessionStateMachine.java`

**验收标准**:
- [ ] 状态转换正确执行
- [ ] 状态历史可查询
- [ ] 非法事件被拒绝

---

### 任务 5: 数据库设计与实现

**目标**: 创建数据库表结构和 MyBatis 映射，基于 Spring Boot

**子任务**:
5.1 创建数据库初始化脚本
   - 创建 `flow_session` 表
   - 创建 `flow_state_history` 表
   - 创建 `storage_file` 表
   - 创建 `plugin_instance` 表
   - **使用 Flyway 或 Liquibase 管理数据库迁移**

5.2 定义 MyBatis 实体类
   - `FlowSession`
   - `FlowStateHistory`
   - `StorageFile`
   - `PluginInstance`

5.3 创建 MyBatis XML 映射文件
   - `FlowSessionMapper.xml`
   - `FlowStateHistoryMapper.xml`
   - `StorageFileMapper.xml`
   - `PluginInstanceMapper.xml`

5.4 定义 Mapper 接口
   - `FlowSessionMapper`
   - `FlowStateHistoryMapper`
   - `StorageFileMapper`
   - `PluginInstanceMapper`

5.5 配置 MyBatis + Spring Boot
   - **使用 `mybatis-spring-boot-starter`**
   - `application.yml` 配置数据源、MyBatis
   - **使用 `@MapperScan` 扫描 Mapper 接口**

**交付物**:
- `aidrclaw-core/src/main/resources/db/migration/V1__init.sql`
- `aidrclaw-core/src/main/java/com/aidrclaw/core/entity/` 实体类
- `aidrclaw-core/src/main/resources/mappers/` XML 映射文件
- Mapper 接口
- **`application.yml` Spring Boot 配置文件**

**验收标准**:
- [ ] 数据库表正确创建
- [ ] MyBatis + Spring Boot 配置正确
- [ ] CRUD 操作正常工作
- [ ] 单元测试覆盖
- [ ] Flyway/Liquibase 迁移成功执行

---

### 任务 6: 实现基础存储插件

**目标**: 实现本地磁盘存储插件

**子任务**:
6.1 实现 `StoragePlugin` 接口
   - `save(InputStream data, StorageMetadata metadata)`: 保存文件
   - `load(String path)`: 读取文件
   - `delete(String path)`: 删除文件

6.2 实现文件存储逻辑
   - 计算文件 SHA-256 哈希值
   - 存储到配置的目录
   - 记录元数据到数据库

6.3 实现 `StorageMetadata` 类
   - 文件路径
   - 文件大小
   - 文件哈希值
   - 创建时间
   -  MIME 类型

6.4 配置文件存储路径
   - YAML 配置 `storage.local.base-path`
   - 支持相对路径和绝对路径

**交付物**:
- `aidrclaw-plugins-storage/src/main/java/com/aidrclaw/plugin/storage/` 目录
- `StoragePluginImpl.java`, `StorageMetadata.java`
- 配置文件示例

**验收标准**:
- [ ] 文件可正确保存到磁盘
- [ ] 文件可正确读取
- [ ] 元数据正确记录到数据库
- [ ] 文件哈希值正确计算

---

### 任务 7: 集成测试

**目标**: 验证整个系统正常工作

**子任务**:
7.1 编写插件加载测试
   - 测试 ServiceLoader 发现插件
   - 测试插件生命周期调用

7.2 编写状态机测试
   - 测试所有状态转换
   - 测试非法事件处理

7.3 编写存储插件测试
   - 测试文件保存和读取
   - 测试元数据持久化

7.4 编写集成测试
   - 端到端测试：加载插件 → 初始化会话 → 存储文件
   - 验证数据库记录正确

**交付物**:
- 各模块的单元测试文件
- `aidrclaw-core/src/test/java/com/aidrclaw/core/integration/` 集成测试
- 测试报告

**验收标准**:
- [ ] 所有单元测试通过
- [ ] 所有集成测试通过
- [ ] 代码覆盖率 ≥ 80%

---

## 任务依赖关系

```
任务 1 (Maven 结构)
    ↓
任务 2 (插件 API) → 任务 3 (核心引擎) → 任务 4 (状态机)
                                    ↓
                              任务 5 (数据库) → 任务 6 (存储插件)
                                    ↓
                              任务 7 (集成测试)
```

---

## 研究需求

**需要研究的领域**:
1. Java SPI 最佳实践和常见陷阱
2. **Spring Boot 3.x 插件化架构最佳实践**
3. **Spring ApplicationEventPublisher vs Guava EventBus**
4. MyBatis XML 映射配置最佳实践 (**mybatis-spring-boot-starter**)
5. Maven 多模块项目依赖管理
6. PostgreSQL 连接池配置优化 (**HikariCP + Spring Boot**)
7. **Spring Boot 配置外部化最佳实践**

---

## 验证清单

规划完成后，验证以下内容：

- [ ] 所有任务都有明确的交付物
- [ ] 任务依赖关系清晰
- [ ] 验收标准可测试
- [ ] 研究问题已识别
- [ ] 任务估计合理

---

*阶段：01-basic-architecture*
*计划创建：2026 年 3 月 24 日*
