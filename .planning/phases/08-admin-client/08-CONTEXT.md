# Phase 08 CONTEXT - 管理后台与客户端

**阶段号**: 8  
**创建时间**: 2026 年 3 月 25 日  
**状态**: ⏳ 待规划

---

## 一栏总结

实现管理后台（插件管理、流程设计器、录制记录查询、质检复核）和客户端（双录邀请、客户引导界面、异常提示）

---

## 需求映射

| 需求 ID | 需求描述 | 实现方式 |
|---------|----------|----------|
| ADMIN-01 | 插件市场展示已安装插件，支持启用/停用、配置参数、版本升级 | 管理后台 - 插件管理页面 |
| ADMIN-02 | 流程设计器支持拖拽式编排双录流程，绑定 Agent 节点和质检规则 | 管理后台 - 流程设计器（React Flow） |
| ADMIN-03 | 录制记录查询按客户、时间、产品、合规状态等检索，支持音视频在线回放 | 管理后台 - 录制记录页面 |
| ADMIN-04 | 质检管理展示预质检结果列表，支持人工复核、修改结论、添加备注 | 管理后台 - 质检复核页面 |
| CLIENT-01 | 支持理财经理生成双录邀请（二维码/链接），客户扫码或点击链接进入 | 客户端 - 邀请生成 |
| CLIENT-02 | 客户引导界面支持虚拟坐席以视频或语音形式引导客户完成流程 | 客户端 - 双录界面 |
| CLIENT-03 | 异常提示（遮挡、离席、噪音过大）时 Agent 主动提示客户调整 | 客户端 - 实时质检提示 |

---

## 设计决策

### 决策 1：管理后台技术栈

**决策**: React 18 + TypeScript + Tailwind CSS + shadcn/ui

**理由**:
- 与 Phase 2 客户端技术栈一致
- shadcn/ui 提供高质量组件
- Tailwind CSS 快速样式开发

**影响**:
- researcher 需研究 shadcn/ui 组件库
- planner 需包括前端组件实现任务

### 决策 2：流程设计器实现

**决策**: React Flow（基于 Web 的流程图库）

**理由**:
- 开源免费（MIT 许可）
- 支持拖拽、缩放、连接节点
- 丰富的示例和文档
- 适合双录流程可视化编排

**影响**:
- researcher 需研究 React Flow API
- planner 需包括流程设计器组件实现任务

### 决策 3：音视频回放

**决策**: HTML5 `<video>` 标签 + HLS.js（如需要）

**理由**:
- 浏览器原生支持 MP4 格式
- HLS.js 支持流式播放（如使用 HLS）
- 无需额外插件

**影响**:
- 后端需提供视频流 API
- 前端需实现播放器组件

### 决策 4：客户端入口

**决策**: 单页面应用（SPA）+ 动态路由

**理由**:
- 理财经理生成邀请链接（如 `/record/{sessionId}`）
- 客户点击链接直接进入双录界面
- 无需登录（基于会话 Token）

**影响**:
- 需实现会话 Token 验证
- 前端需支持动态路由参数

---

## 架构复用

### Phase 01-07 资产复用

| 组件 | 用途 |
|------|------|
| PluginManager（Phase 1） | 插件市场展示和管理 |
| ProcessEngine（Phase 5） | 流程设计器生成的流程定义 |
| DialogTreeManager（Phase 6） | 流程设计器绑定对话树 |
| QualityInspectionAgent（Phase 7） | 质检复核页面展示质检结果 |
| SensitiveWordService（Phase 7） | 质检复核页面展示违规词 |

### 新增组件

| 组件 | 职责 |
|------|------|
| AdminDashboard | 管理后台主界面（路由、布局） |
| PluginMarket | 插件市场页面（列表、启用/停用、配置） |
| FlowDesigner | 流程设计器（React Flow 画布、节点配置） |
| RecordList | 录制记录列表（搜索、筛选、回放） |
| QualityReview | 质检复核页面（列表、详情、修改结论） |
| ClientRecord | 客户端双录界面（虚拟坐席、实时提示） |
| InviteGenerator | 邀请生成器（二维码、链接） |

---

## 核心组件设计

### 管理后台

#### AdminDashboard
```typescript
// 管理后台主路由
const routes = [
  { path: '/admin/plugins', component: PluginMarket },
  { path: '/admin/flows', component: FlowDesigner },
  { path: '/admin/records', component: RecordList },
  { path: '/admin/quality', component: QualityReview },
];
```

#### PluginMarket
```typescript
interface PluginInfo {
  id: string;
  name: string;
  version: string;
  enabled: boolean;
  config: Record<string, any>;
}
```

#### FlowDesigner
```typescript
// 使用 React Flow 实现拖拽式流程设计
interface FlowNode {
  id: string;
  type: 'identity' | 'risk' | 'product' | 'confirmation';
  position: { x: number; y: number };
  data: { label: string; agentConfig?: any; qualityRules?: any[] };
}
```

### 客户端

#### ClientRecord
```typescript
// 客户端双录界面组件
interface ClientRecordProps {
  sessionId: string;
  token: string;
}

// 虚拟坐席集成（Phase 6）
<VirtualAgentInterface sessionId={sessionId} />
```

#### InviteGenerator
```typescript
// 邀请链接生成
const inviteUrl = `https://yourapp.com/record/${sessionId}?token=${token}`;

// 二维码生成（使用 qrcode.react）
<QRCode value={inviteUrl} />
```

---

## 技术栈

| 技术 | 用途 | 理由 |
|------|------|------|
| React 18 + TypeScript | 前端框架 | 与 Phase 2 一致 |
| Tailwind CSS | 样式 | 快速开发 |
| shadcn/ui | UI 组件库 | 高质量、可定制 |
| React Flow | 流程设计器 | 开源、功能丰富 |
| qrcode.react | 二维码生成 | 简单易用 |
| React Router | 路由管理 | SPA 标准方案 |
| Axios | HTTP 客户端 | 与后端 API 通信 |

---

## 成功标准

1. ✅ 管理后台可展示和管理已安装插件
2. ✅ 可通过拖拽方式设计双录流程
3. ✅ 可按多维度查询录制记录
4. ✅ 支持在线回放音视频
5. ✅ 合规管理员可复核质检结果并修改结论
6. ✅ 理财经理可生成双录邀请链接/二维码
7. ✅ 客户可通过链接进入并完成双录流程
8. ✅ 异常情况（遮挡、离席、噪音）主动提示客户

---

## 下游代理指南

### gsd-phase-researcher

**研究重点**:
1. shadcn/ui 组件库的使用和定制
2. React Flow 流程设计器实现方案
3. 音视频播放器实现（HTML5 video + HLS.js）
4. 二维码生成方案（qrcode.react）
5. 会话 Token 验证方案

**无需研究**:
- ❌ 后端 API 实现（已有 Phase 01-07 基础）
- ❌ 虚拟坐席实现（Phase 6 已完成）

### gsd-planner

**计划重点**:
1. 管理后台路由和布局
2. 插件市场页面实现
3. 流程设计器实现（React Flow）
4. 录制记录查询页面
5. 质检复核页面
6. 客户端双录界面
7. 邀请生成器

**任务依赖**:
- 依赖 Phase 1：PluginManager
- 依赖 Phase 5：ProcessEngine
- 依赖 Phase 6：VirtualAgentInterface
- 依赖 Phase 7：QualityInspectionAgent

---

## Deferred Ideas（延至 Phase 09 或 V2）

| 功能 | 原因 | Phase |
|------|------|-------|
| 流程设计器可视化（拖拽） | MVP 使用 YAML 配置，V2 实现可视化 | Phase 09 |
| 实时质检提示 | MVP 使用录制后质检，V2 实现实时 | Phase 09 |
| 移动端 App | MVP 使用 Web 端，V2 实现原生 App | V2 |
| 多租户支持 | V2.0 需求 | V2 |

---

## 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| React Flow 学习曲线 | 中 | 参考官方示例，从简单开始 |
| 音视频播放兼容性 | 中 | 使用标准 MP4 格式，充分测试 |
| 会话 Token 安全 | 高 | 使用 JWT，设置短有效期 |
| 二维码扫描体验 | 低 | 提供链接备用方案 |

---

## 下一步

**执行命令**: `/gsd:research-phase 8` 或 `/gsd:plan-phase 8`

**输入**: 本 CONTEXT.md 文件

**输出**:
- researcher: RESEARCH.md（shadcn/ui、React Flow、音视频播放研究）
- planner: 08-XX-PLAN.md（实现任务计划）

---

*Created: 2026 年 3 月 25 日 - Phase 8 待规划*
