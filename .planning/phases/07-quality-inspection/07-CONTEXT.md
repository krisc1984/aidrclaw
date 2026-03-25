# Phase 07 CONTEXT - 质检 Agent 与合规检测

**阶段号**: 7  
**创建时间**: 2026 年 3 月 25 日  
**状态**: ✅ 设计已批准

---

## 一栏总结

实现质检 Agent，在录制完成后自动检测话术完整性和违规词汇，输出简单评分报告（0-100 分 + 通过/不通过）

---

## 需求映射

| 需求 ID | 需求描述 | 实现方式 |
|---------|----------|----------|
| AGENT-03 | 质检 Agent 录制完成后自动调用质检插件分析音视频 | QualityInspectionAgent 同步触发 |
| AI-05 | 合规检测插件检测违规词汇、违规动作、画面合规 | 关键词匹配（MVP），动作/画面检测延至 Phase 08 |

---

## 设计决策

### 决策 1：质检触发时机

**决策**: 录制完成立即触发（同步）

**理由**:
- 用户体验优先，质检结果立即可用
- 不阻塞后续操作（质检在后台完成）
- 符合 MVP 定位

**影响**:
- researcher 需研究同步触发的最佳实践
- planner 需包括状态监听和触发逻辑

---

### 决策 2：质检范围

**决策**: 话术完整性 + 违规词汇检测（必需）

**话术完整性检测**:
- 基于 DialogState.history 判断
- 检查对话树所有必需节点是否完成
- 返回：完成率%

**违规词汇检测**:
- 基于关键词匹配（简单高效）
- 内置敏感词库（保本、保收益、绝对收益等）
- 返回：违规词列表

**暂不实现**（Phase 08）:
- ❌ 违规动作检测（吸烟、接电话）- 需要 CV 模型
- ❌ 画面合规检测（光线、遮挡）- 需要 CV 模型

**影响**:
- researcher 需研究关键词匹配的最佳实践
- planner 需包括敏感词库配置功能

---

### 决策 3：质检报告格式

**决策**: 简单评分（MVP）

**报告结构**:
```java
public class QualityReport {
    private Integer score;              // 0-100 分
    private Boolean passed;             // 是否通过
    private String reason;              // 不通过原因
    private List<String> violations;    // 违规词列表
}
```

**影响**:
- researcher 无需研究复杂报告格式
- planner 需包括评分计算逻辑

---

## 架构复用

### Phase 05 资产复用

| 组件 | 用途 |
|------|------|
| ProcessEngine | 执行质检流程 |
| DialogTreeManager | 加载质检话术树、获取对话状态 |
| DialogState | 对话状态和历史记录 |

### Phase 06 资产复用

| 组件 | 用途 |
|------|------|
| AgentRulesEngine | 执行质检规则（SpEL） |
| BusinessRule | 质检规则数据模型 |
| DecisionContext/Result | 质检结果模型可复用 |

---

## 核心组件设计

### QualityInspectionAgent

**职责**: 质检 Agent 核心，协调质检流程

**依赖**:
- ProcessEngine（Phase 05）
- AgentRulesEngine（Phase 06）
- DialogTreeManager（Phase 05）

**方法**:
```java
public class QualityInspectionAgent {
    public QualityReport inspect(String sessionId);
    private boolean checkScriptCompleteness(DialogState state);
    private List<String> detectViolations(String transcript);
    private QualityReport buildReport(DialogState state);
}
```

### ScriptCompletenessRule

**职责**: 话术完整性检测规则

**逻辑**:
1. 获取对话树所有必需节点
2. 检查 DialogState.history 包含所有必需节点
3. 计算完成率 = 已完成节点 / 必需节点总数
4. 返回：完成率%

### ViolationKeywordsRule

**职责**: 违规词汇检测规则

**逻辑**:
1. 加载敏感词库（从配置文件或数据库）
2. 遍历对话历史文本
3. 关键词匹配
4. 返回：违规词列表

### QualityReport

**职责**: 质检报告数据模型

**字段**:
- score: Integer - 0-100 分
- passed: Boolean - 是否通过
- reason: String - 不通过原因
- violations: List<String> - 违规词列表

---

## 技术栈

| 技术 | 用途 | 理由 |
|------|------|------|
| Java + Spring Boot 3 | 主技术栈 | 与 Phase 05-06 一致 |
| SpEL 规则引擎 | 质检规则执行 | 复用 Phase 06 实现 |
| 关键词匹配 | 违规检测 | MVP 简单高效 |
| 对话树状态管理 | 话术完整性检测 | 复用 Phase 05 实现 |

---

## 成功标准

1. ✅ 录制完成后自动触发质检流程
2. ✅ 质检 Agent 可检测话术完整性
3. ✅ 可检测违规词汇（保本、保收益等）
4. ✅ 生成简单评分报告（0-100 分 + 通过/不通过）
5. ✅ 复用 Phase 05-06 的流程引擎和规则引擎
6. ✅ 单元测试覆盖率 ≥ 80%

---

## 下游代理指南

### gsd-phase-researcher

**研究重点**:
1. 关键词匹配的最佳实践（准确率、性能）
2. 敏感词库管理方案（配置、更新）
3. 质检评分计算算法（权重、阈值）
4. 同步触发的并发处理方案

**无需研究**:
- ❌ CV 模型（违规动作、画面检测）
- ❌ NLP 模型（语义分析）
- ❌ 复杂报告格式

### gsd-planner

**计划重点**:
1. QualityInspectionAgent 实现任务
2. ScriptCompletenessRule 实现任务
3. ViolationKeywordsRule 实现任务
4. QualityReport 模型创建任务
5. 敏感词库配置任务
6. 单元测试任务

**任务依赖**:
- 依赖 Phase 05：ProcessEngine, DialogTreeManager
- 依赖 Phase 06：AgentRulesEngine, BusinessRule

---

##  Deferred Ideas（延至 Phase 08）

| 功能 | 原因 | Phase |
|------|------|-------|
| 违规动作检测（吸烟、接电话） | 需要 CV 模型，超出 MVP 范围 | Phase 08 |
| 画面合规检测（光线、遮挡） | 需要 CV 模型，超出 MVP 范围 | Phase 08 |
| 详细质检报告（时间戳定位） | MVP 简单评分即可 | Phase 08 |
| NLP 语义分析 | 关键词匹配已满足需求 | Phase 08 |

---

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 关键词匹配误报率高 | 质检准确率下降 | 建立词库审核机制，支持白名单 |
| 同步触发阻塞用户操作 | 用户体验下降 | 优化质检性能，设置超时时间 |
| 敏感词库维护成本高 | 运营负担 | 提供词库管理界面（Phase 08） |

---

## 下一步

**执行命令**: `/gsd:research-phase 7` 或 `/gsd:plan-phase 7`

**输入**: 本 CONTEXT.md 文件

**输出**:
- researcher: RESEARCH.md（关键词匹配、评分算法研究）
- planner: 07-XX-PLAN.md（实现任务计划）

---

*Last updated: 2026 年 3 月 25 日 - 设计已批准*
