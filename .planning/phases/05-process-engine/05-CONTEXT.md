# Phase 5: 流程引擎 - Context

**Gathered:** 2026 年 3 月 25 日  
**Status:** Ready for planning

<domain>
## Phase Boundary

**交付目标**: 实现双录流程编排引擎，支持流程定义、执行和动态跳转

**本阶段包括**:
- 流程定义（YAML 配置文件方式）
- 流程引擎（执行流程节点）
- 会话状态机（每会话独立实例）
- 规则引擎（轻量级脚本，支持条件跳转）
- 超时处理和重试机制

**本阶段不包括**:
- 虚拟坐席 Agent 对话逻辑 — Phase 6
- 质检规则配置 — Phase 7
- 流程设计器 UI（可视化拖拽）— Phase 8
- AI 意图识别和动态判断 — Phase 6

</domain>

<decisions>
## Implementation Decisions

### 流程定义方式

**决策**: 使用 YAML 配置文件定义流程

**理由**:
- 简洁易读，便于版本控制
- 符合 Phase 5 技术聚焦
- Phase 8 可视化设计器可基于相同结构

**配置示例**:
```yaml
process:
  id: wealth-product-recording
  name: 理财产品双录流程
  nodes:
    - id: identity-verification
      name: 身份核验
      type: AI_NODE
      plugin: face-plugin
      next: risk-disclosure
      
    - id: risk-disclosure
      name: 风险揭示
      type: AGENT_NODE
      script: risk-script-001
      next: product-introduction
      
    - id: product-introduction
      name: 产品介绍
      type: AGENT_NODE
      script: product-script-001
      next: confirmation
      
    - id: confirmation
      name: 确认签字
      type: MANUAL_NODE
      next: complete
```

### 状态机设计

**决策**: 每会话独立状态机实例

**理由**:
- 会话间状态隔离，互不影响
- 支持高并发（100+ 并发会话）
- 易于调试和追踪
- Phase 1 已有 `SessionStateMachine`，需改造为每会话实例

**架构**:
```java
public class SessionStateMachine {
    private final String sessionId;
    private SessionState currentState;
    
    public void transition(SessionState newState) {
        // 只影响当前会话
    }
}

public class SessionManager {
    private Map<String, SessionStateMachine> sessions = new ConcurrentHashMap<>();
    
    public SessionStateMachine getSession(String sessionId) {
        return sessions.computeIfAbsent(sessionId, id -> new SessionStateMachine(id));
    }
}
```

### 规则引擎选型

**决策**: 轻量级脚本引擎（JavaScript/Groovy）

**理由**:
- 表达能力强，支持复杂逻辑
- 无需引入重型依赖（如 Drools）
- Java 8+ 自带 Nashorn（或 GraalVM）
- 团队容易上手

**配置示例**:
```yaml
nodes:
  - id: face-verification
    name: 人脸核验
    transitions:
      - condition: "result.similarity > 0.6"
        next: risk-disclosure
      - condition: "result.similarity <= 0.6 && retryCount < 3"
        next: face-verification
        action: "retryCount++"
      - condition: "result.similarity <= 0.6 && retryCount >= 3"
        next: manual-review
```

### 流程跳转策略

**决策**: 预定义规则 + AI 判断混合模式

**Phase 5 范围**:
- 支持预定义规则（阈值、计数、超时）
- 示例：
  - `faceResult.similarity > 0.6`
  - `timeout > 300` (5 分钟超时)
  - `retryCount >= 3`

**Phase 6 预留接口**:
- AI 意图识别：`aiIntent == 'CONFIRM'`
- 情绪分析：`emotion == 'CONFUSED'`

**理由**:
- 符合 ROADMAP 阶段划分
- Phase 5 聚焦规则驱动，技术可行
- 预留 AI 接口，Phase 6 可平滑升级

### 会话状态定义

**决策**: 采用以下状态枚举

```java
public enum SessionState {
    IDLE,              // 待开始
    INITIALIZING,      // 初始化中
    IDENTITY_CHECK,    // 身份核验
    RECORDING,         // 录制中
    INSPECTING,        // 预质检中
    COMPLETED,         // 完成
    EXCEPTION,         // 异常
    MANUAL_REVIEW      // 人工复核
}
```

### 节点类型定义

**决策**: 支持以下节点类型

```yaml
node_types:
  - AI_NODE        # AI 处理节点（如人脸核验）
  - AGENT_NODE     # 虚拟坐席节点（话术播报）
  - MANUAL_NODE    # 人工操作节点（如签字确认）
  - CONDITION_NODE # 条件判断节点
  - END_NODE       # 结束节点
```

</decisions>

<code_context>
## Existing Code Insights

### Reusable Assets

**Phase 1 交付**:
- `SessionStateMachine.java` - 需改造为每会话实例
- `SessionState.java` - 状态枚举，可直接复用
- `FlowStateHistory.java` - 状态历史记录，可直接复用
- `PluginManager.java` - 插件加载和管理，可直接复用

**Phase 4 交付**:
- `AsrPlugin` - ASR 插件，流程引擎可调用
- `FacePlugin` - 人脸比对插件，流程引擎可调用
- `TtsPlugin` - TTS 插件，流程引擎可调用

### Established Patterns

**插件模式**:
- 所有 AI 能力以插件形式实现
- 流程引擎通过 `PluginManager.executePlugin()` 调用插件

**配置模式**:
- 使用 `@ConfigurationProperties` 绑定 YAML 配置
- 支持环境变量覆盖

**错误处理**:
- 使用 `PluginResult.error()` 返回错误
- 流程引擎需捕获异常并触发异常流程

### Integration Points

**需要集成的组件**:
1. `PluginManager` - 调用 AI 插件（人脸、ASR、TTS）
2. `SessionManager` - 管理会话生命周期
3. `FlowStateHistory` - 记录状态流转历史
4. `StorageOrchestrator` - 保存流程执行结果

**需要新增的组件**:
1. `ProcessDefinition` - 流程定义解析
2. `ProcessEngine` - 流程执行引擎
3. `RuleEngine` - 脚本规则引擎
4. `TransitionManager` - 流转管理

</code_context>

<specifics>
## Specific Ideas

### 流程配置位置

**决策**: 配置文件放在 `src/main/resources/processes/` 目录

```
src/main/resources/processes/
  ├── wealth-product-recording.yml  # 理财产品双录流程
  ├── fund-product-recording.yml    # 基金产品双录流程
  └── insurance-recording.yml       # 保险双录流程
```

### 规则脚本上下文

**决策**: 脚本执行时提供以下上下文变量

```javascript
// 可用变量
result          // 当前节点执行结果
session         // 会话信息（sessionId, customerId, productId）
retryCount      // 当前节点重试次数
timeout         // 当前节点已耗时（秒）
aiIntent        // AI 意图（Phase 6 预留）
emotion         // 情绪分析（Phase 6 预留）

// 示例脚本
result.similarity > 0.6 && retryCount < 3
```

### 超时处理

**决策**: 每个节点支持超时配置

```yaml
nodes:
  - id: face-verification
    name: 人脸核验
    timeout: 300  # 5 分钟超时
    onTimeout: timeout-reminder  # 超时后跳转到提醒节点
```

### 重试机制

**决策**: 支持节点级重试配置

```yaml
nodes:
  - id: face-verification
    name: 人脸核验
    maxRetries: 3
    retryDelay: 5000  # 5 秒后重试
```

</specifics>

<deferred>
## Deferred Ideas

以下想法在讨论中被提出，但属于后续阶段范围：

1. **可视化流程设计器** - Phase 8（管理后台）
   - 拖拽式流程编排界面
   - 实时预览流程效果

2. **AI 意图识别动态跳转** - Phase 6（Agent 智能体域）
   - 基于客户回答自动判断下一步
   - 情绪识别和应对

3. **质检规则配置** - Phase 7（质检 Agent）
   - 质检规则可视化配置
   - 违规检测阈值调整

4. **流程版本管理** - Phase 8（管理后台）
   - 流程版本历史
   - 灰度发布和回滚

</deferred>

---

*Phase: 05-process-engine*  
*Context gathered: 2026 年 3 月 25 日*
