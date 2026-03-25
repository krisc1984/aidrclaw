# Phase 6: Agent 智能体域 - 研究报告

## 执行摘要

基于对规则引擎、对话管理系统和 Agent 架构的研究，Phase 6 推荐采用以下技术方案：

1. **规则引擎**: **EasyRules** (轻量级，适合规则驱动的话术引导)
2. **对话管理**: **对话树 (Dialog Tree)** + 状态机混合模式
3. **Agent 架构**: 单 Agent 多工具模式 (Single Agent with Tools)
4. **界面方案**: React + TypeScript 虚拟坐席对话界面

---

## 规则引擎选型研究

### 候选方案对比

| 引擎 | 类型 | 学习曲线 | 性能 | 社区支持 | 适用场景 | 推荐度 |
|------|------|----------|------|----------|----------|--------|
| **Drools** | 重量级 BRMS | 陡峭 | 高 | 强 (Red Hat) | 复杂决策系统、百条以上规则 | ⭐⭐⭐ |
| **EasyRules** | 轻量级框架 | 平缓 | 中 | 中 (Apache) | 简单规则、20-100 条规则 | ⭐⭐⭐⭐⭐ |
| **LiteFlow** | 流程式引擎 | 中等 | 高 | 中 (国产) | 复杂业务流程、编排场景 | ⭐⭐⭐⭐ |
| **OpenL Tablets** | 决策表驱动 | 中等 | 中 | 中 | 业务人员可编辑的规则 | ⭐⭐⭐ |

### 推荐方案：EasyRules

**理由**:
1. ✅ **轻量级**: jar < 100KB，无第三方依赖，符合 V1.0 简洁原则
2. ✅ **注解驱动**: 基于 POJO，开发门槛低，团队易上手
3. ✅ **复合规则**: 支持规则组合，适合话术流程的层级结构
4. ✅ **表达式语言**: 支持 SpEL，可灵活定义条件判断
5. ✅ **性能足够**: 双录话术规则预计 50-80 条，EasyRules 完全胜任

**不选 Drools 的原因**:
- ❌ 学习曲线陡峭，需要专门团队维护
- ❌ 功能过度 (DRL/DSL 等多种语法选择造成复杂性)
- ❌ 部署复杂 (需要 Workbench 等配套工具)
- ❌ 隐藏成本高 (虽然开源，但实施成本超过轻量级方案)

### EasyRules 核心概念

```java
// 规则定义示例
@Rule(name = "风险揭示确认规则", description = "客户必须确认理解风险揭示")
public class RiskDisclosureRule {
    
    @Condition
    public boolean whenCustomerNotConfirmed(@Fact("session") Session session) {
        return !session.isRiskDisclosureConfirmed();
    }
    
    @Action
    public void thenAskForConfirmation(@Fact("session") Session session) {
        session.setNextQuestion("您是否已理解以上投资风险？请回答'是'或'否'");
    }
}

// 规则引擎使用
Rules rules = new Rules();
rules.register(new RiskDisclosureRule());

RulesEngine engine = new DefaultRulesEngine();
engine.fire(rules, session);
```

---

## 对话管理系统研究

### 对话管理方法对比

| 方法 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| **有限状态机 (FSM)** | 结构简单、易调试、可预测 | 状态爆炸、扩展性差、路径固定 | 简单流程 (<20 状态) |
| **对话树 (Dialog Tree)** | 层级清晰、支持分支合并、易维护 | 深度过大时复杂、不支持动态跳转 | 话术引导、客服流程 |
| **基于框架 (Frame-Based)** | 适合信息收集、槽位填充 | 对话不自然、机械感强 | 预订、注册等表单场景 |
| **概率模型 (MDP/POMDP)** | 支持不确定性、灵活 | 需要训练数据、复杂度高 | 开放域对话 |
| **混合方法 (Hybrid)** | 结合规则 + ML、平衡灵活与可控 | 实现复杂度高 | 复杂生产系统 |

### 推荐方案：对话树 + FSM 混合模式

**架构设计**:

```
对话树 (高层结构)
├─ 身份核验分支 (FSM 管理)
│  ├─ 状态 1: 问候
│  ├─ 状态 2: 身份证采集
│  ├─ 状态 3: 人脸比对
│  └─ 状态 4: 活体检测
├─ 风险揭示分支 (FSM 管理)
│  ├─ 状态 1: 风险等级告知
│  ├─ 状态 2: 关键条款讲解
│  ├─ 状态 3: 客户确认
│  └─ 状态 4: 疑问解答
└─ 产品介绍分支 (FSM 管理)
   ├─ 状态 1: 产品基本信息
   ├─ 状态 2: 收益与风险
   ├─ 状态 3: 费用说明
   └─ 状态 4: 客户确认

状态机 (低层流转)
[State] --(条件)--> [Next State]
   │
   ├─ 条件：客户回答
   ├─ 条件：超时
   └─ 条件：异常事件
```

**为什么选择混合模式**:
1. ✅ **对话树** 管理高层分支 (身份核验 → 风险揭示 → 产品介绍)，支持可视化配置
2. ✅ **FSM** 管理每个分支内的详细状态流转，保证可控性
3. ✅ **支持分支切换**: 客户提问时可临时跳转到 FAQ 分支，然后返回原分支
4. ✅ **支持合并**: 不同路径可汇聚到同一节点 (如确认环节)

### 对话树数据结构

```typescript
interface DialogNode {
  id: string;
  type: 'message' | 'question' | 'action' | 'merge' | 'terminal';
  content: string;           // TTS 播报内容
  question?: {
    expectedAnswers: string[]; // 期望回答模式 ['是', '否']
    intentMap: Record<string, string>; // 意图→下一节点映射
    timeoutSeconds: number;    // 超时时间
    maxRetries: number;        // 最大重试次数
  };
  action?: {
    pluginName: string;        // 调用的插件名称
    methodName: string;        // 插件方法
    params: Record<string, any>; // 参数
  };
  children: Record<string, string>; // 意图→子节点 ID 映射
  parent?: string;              // 父节点 ID (用于返回)
}

interface DialogTree {
  id: string;
  name: string;
  rootNodeId: string;
  nodes: Record<string, DialogNode>;
  metadata: {
    productId?: string;        // 适用产品 ID
    riskLevel?: string;        // 适用风险等级
    version: string;
  };
}
```

---

## Agent 架构研究

### Agent 设计模式对比

| 模式 | 描述 | 优点 | 缺点 | 适用场景 |
|------|------|------|------|----------|
| **单 Agent** | 一个 Agent 处理所有任务 | 简单、易调试、低延迟 | 复杂任务性能下降 | 结构化任务、单一领域 |
| **多 Agent 协作** | 多个专业 Agent 分工 | 专业化、可扩展 | 协调开销、复杂度高 | 跨领域复杂任务 |
| **分层 Agent** | 父 Agent 分配任务给子 Agent | 模块化、职责清晰 | 实现复杂 | 企业级复杂流程 |
| **路由 Agent** | 路由 Agent 分发到专业 Agent | 灵活、专业化 | 需要路由逻辑 | 多主题客服 |

### 推荐方案：单 Agent 多工具模式

**架构设计**:

```
┌─────────────────────────────────────────┐
│         虚拟坐席 Agent                   │
│  (Virtual Agent Service)                │
├─────────────────────────────────────────┤
│  对话管理器 (Dialog Manager)             │
│  ├─ 对话树解析器                         │
│  ├─ 状态跟踪器 (DST)                     │
│  └─ 规则引擎 (EasyRules)                │
├─────────────────────────────────────────┤
│  工具层 (Tools)                          │
│  ├─ TTS 插件 (语音合成)                  │
│  ├─ ASR 插件 (语音识别)                  │
│  ├─ 人脸比对插件                         │
│  ├─ 流程引擎 API                         │
│  └─ 会话存储服务                         │
└─────────────────────────────────────────┘
```

**为什么选择单 Agent**:
1. ✅ **双录流程高度结构化**: 不需要多 Agent 的灵活性
2. ✅ **低延迟要求**: 减少 Agent 间通信开销
3. ✅ **易调试**: 单一责任，问题定位简单
4. ✅ **V1.0 范围**: 规则驱动即可满足需求，不需要复杂推理

**工具集成方式**:

```java
@Service
public class VirtualAgentService {
    
    @Autowired
    private DialogTreeManager dialogTreeManager;
    
    @Autowired
    private TtsPlugin ttsPlugin;
    
    @Autowired
    private AsrPlugin asrPlugin;
    
    @Autowired
    private FaceRecognitionPlugin facePlugin;
    
    @Autowired
    private RulesEngine rulesEngine;
    
    public AgentResponse processTurn(Session session, UserInput input) {
        // 1. 更新对话状态
        DialogState state = dialogTreeManager.getCurrentState(session);
        
        // 2. 应用规则引擎判断下一步
        rulesEngine.fire(getRulesForState(state), session);
        
        // 3. 根据意图决定下一节点
        String nextNodeId = determineNextNode(state, input);
        
        // 4. 获取下一节点内容
        DialogNode nextNode = dialogTreeManager.getNode(nextNodeId);
        
        // 5. 生成响应 (文本 + 语音)
        AgentResponse response = new AgentResponse();
        response.setText(nextNode.getContent());
        response.setAudio(ttsPlugin.synthesize(nextNode.getContent()));
        
        return response;
    }
}
```

---

## 流程决策 Agent 设计

### 业务规则分类

| 规则类型 | 示例 | 存储方式 | 执行引擎 |
|----------|------|----------|----------|
| **流程调整规则** | R3 级以上产品需增加风险揭示环节 | 数据库配置表 | EasyRules |
| **话术适配规则** | 60 岁以上客户语速降低 20% | 数据库配置表 | 动态参数 |
| **条件跳转规则** | 有投资经验客户跳过基础讲解 | 对话树配置 | 对话树分支 |
| **异常处理规则** | 3 次人脸比对失败转人工 | 数据库配置表 | EasyRules |

### 规则数据模型

```java
@Entity
@Table(name = "business_rules")
public class BusinessRule {
    @Id
    private String id;
    
    private String name;
    private String category; // FLOW_ADJUSTMENT, SCRIPT_ADAPTATION, CONDITIONAL_JUMP, EXCEPTION
    
    @Column(columnDefinition = "TEXT")
    private String condition; // SpEL 表达式
    
    @Column(columnDefinition = "TEXT")
    private String action;    // JSON 动作定义
    
    private Integer priority; // 规则优先级
    
    private Boolean enabled;
    
    // 适用条件
    private String minRiskLevel; // 最小风险等级
    private Integer minAge;      // 最小年龄
    private Boolean requireInvestmentExperience; // 需要投资经验
}

// 规则示例数据
{
  "id": "RULE-001",
  "name": "高风险产品增加风险揭示",
  "category": "FLOW_ADJUSTMENT",
  "condition": "session.product.riskLevel >= 'R4'",
  "action": {
    "type": "INSERT_NODE",
    "position": "AFTER_RISK_DISCLOSURE",
    "nodeId": "EXTRA_RISK_WARNING"
  },
  "priority": 100,
  "enabled": true,
  "minRiskLevel": "R4"
}
```

---

## 虚拟坐席界面设计

### 界面组件需求

**必须组件**:
1. **视频区域**: 展示虚拟坐席视频/TTS 动画 (50% 屏幕)
2. **对话气泡**: 显示当前对话内容 (文本)
3. **输入区域**: 
   - 语音按钮 (按住说话)
   - 文本输入框 (可选)
4. **流程进度条**: 显示当前环节和总体进度
5. **异常提示**: 遮挡、离席、噪音等实时提示

### 技术选型

| 组件 | 技术选型 | 理由 |
|------|----------|------|
| **前端框架** | React 18 + TypeScript | 与 Phase 2 保持一致，组件复用 |
| **视频播放** | WebRTC + H.264 | 低延迟，与录制插件一致 |
| **语音播放** | Web Audio API | 原生支持，低延迟 |
| **语音录制** | MediaRecorder API | 与 Phase 2 采集插件复用 |
| **状态管理** | Zustand | 轻量级，适合对话状态管理 |
| **UI 组件库** | shadcn/ui | 现代设计，易定制 |

### 界面布局草图

```
┌─────────────────────────────────────────────────────┐
│ 双录流程：身份核验 → 风险揭示 → 产品介绍 → 确认签字  │
│ ████████████████░░░░░░░░░░░░ 40%                    │
├─────────────────────────────────────────────────────┤
│                                                     │
│           ┌─────────────────┐                       │
│           │                 │                       │
│           │   虚拟坐席视频   │                       │
│           │   (TTS 动画)     │                       │
│           │                 │                       │
│           └─────────────────┘                       │
│                                                     │
│  坐席："您好，请确认您已阅读并理解投资风险揭示书。"  │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │ 🎤 按住说话                                  │   │
│  │ 或输入文字...                               │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
├─────────────────────────────────────────────────────┤
│ ⚠️ 检测到光线不足，请调整环境亮度                    │
└─────────────────────────────────────────────────────┘
```

---

## 关键决策总结

### D-01: 规则引擎选型 ✅
**决策**: **EasyRules**
- 理由：轻量级、注解驱动、适合 V1.0 规则驱动场景
- 备选：LiteFlow (如果后续需要复杂流程编排)

### D-02: 对话管理架构 ✅
**决策**: **对话树 + FSM 混合模式**
- 理由：对话树管理高层分支，FSM 管理底层状态，平衡灵活性与可控性
- 数据结构：JSON 可配置，支持后台管理

### D-03: 流程决策规则存储 ✅
**决策**: **数据库存储 + EasyRules 执行**
- 理由：支持动态配置，管理员可在后台调整规则
- 规则格式：SpEL 表达式 (条件) + JSON (动作)

### D-04: Agent 架构 ✅
**决策**: **单 Agent 多工具模式**
- 理由：双录流程高度结构化，不需要多 Agent 复杂度
- 工具：TTS/ASR/人脸比对通过插件集成

### D-05: 虚拟坐席界面 ✅
**决策**: **React + TypeScript + WebRTC**
- 理由：与 Phase 2 技术栈一致，组件可复用
- 界面：视频 + 对话气泡 + 进度条 + 异常提示

---

## 实现风险与缓解

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| EasyRules 性能不足 | 中 | 低 | 预测试，>100 条规则时切换到 LiteFlow |
| 对话树配置复杂 | 中 | 中 | 提供可视化配置工具 (Phase 8) |
| TTS 延迟过高 | 高 | 中 | 预生成常用话术音频，缓存机制 |
| 意图识别准确率低 | 高 | 中 | V1.0 使用关键词匹配，不依赖 ML |
| 界面与录制插件冲突 | 中 | 低 | 统一 WebRTC 管理，避免资源竞争 |

---

## 参考资源

1. **EasyRules 官方文档**: https://github.com/j-easy/easy-rules
2. **对话状态机最佳实践**: https://djangostars.com/blog/dialog-management-chatbot-development/
3. **Agent 设计模式**: https://docs.cloud.google.com/architecture/choose-design-pattern-agentic-ai-system
4. **Spring AI Agent 集成**: https://www.javacodegeeks.com/designing-intelligent-agents-with-spring-ai.html

---

*Created: 2026 年 3 月 25 日 - Phase 6 规划研究*
