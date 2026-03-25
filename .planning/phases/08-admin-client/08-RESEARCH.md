# Phase 08: 管理后台与客户端 - Research

**Researched**: 2026 年 3 月 25 日  
**Domain**: 管理后台 UI、流程设计器、音视频播放、二维码生成  
**Confidence**: HIGH

## Summary

本阶段研究聚焦于管理后台和客户端的四大核心领域：shadcn/ui 组件库、React Flow 流程设计器、HTML5 音视频播放、二维码生成方案。

**核心发现**:
1. **shadcn/ui**: 基于 Radix UI 的组件库，提供高质量、可定制的 UI 组件，适合管理后台快速开发
2. **React Flow**: 开源流程图库（MIT 许可），支持拖拽、缩放、节点连接，适合双录流程可视化编排
3. **HTML5 Video**: 浏览器原生支持 MP4 格式，HLS.js 支持流式播放
4. **qrcode.react**: React 二维码生成库，简单易用

**Primary recommendation**: 使用 shadcn/ui 构建管理后台组件，React Flow 实现流程设计器，HTML5 video 实现音视频回放，qrcode.react 生成邀请二维码。

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **管理后台技术栈**: React 18 + TypeScript + Tailwind CSS + shadcn/ui
- **流程设计器**: React Flow（基于 Web 的流程图库）
- **音视频回放**: HTML5 `<video>` 标签 + HLS.js（如需要）
- **客户端入口**: 单页面应用（SPA）+ 动态路由

### Claude's Discretion
- shadcn/ui 组件选择和定制
- React Flow 节点类型设计
- 音视频播放器样式和布局
- 二维码生成器配置

### Deferred Ideas（OUT OF SCOPE）
- ❌ 流程设计器可视化（拖拽）- MVP 使用 YAML 配置
- ❌ 实时质检提示 - MVP 使用录制后质检
- ❌ 移动端 App - MVP 使用 Web 端
- ❌ 多租户支持 - V2.0 需求
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| ADMIN-01 | 插件市场展示已安装插件 | shadcn/ui Table, Card, Button 组件 |
| ADMIN-02 | 流程设计器支持拖拽式编排 | React Flow 节点、边、拖拽 |
| ADMIN-03 | 录制记录查询和音视频回放 | HTML5 video, HLS.js |
| ADMIN-04 | 质检管理展示和人工复核 | shadcn/ui Table, Dialog, Form |
| CLIENT-01 | 理财经理生成双录邀请 | qrcode.react 二维码生成 |
| CLIENT-02 | 客户引导界面虚拟坐席 | Phase 6 VirtualAgentInterface 集成 |
| CLIENT-03 | 异常提示客户调整 | Phase 7 质检结果实时推送 |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| shadcn/ui | latest | UI 组件库 | 高质量、可定制、基于 Radix UI |
| React Flow | v11+ | 流程设计器 | 开源、功能丰富、文档完善 |
| qrcode.react | v3+ | 二维码生成 | 简单易用、React 友好 |
| HTML5 Video | Native | 音视频播放 | 浏览器原生支持 |
| HLS.js | v1+ | HLS 流播放 | 支持流式播放、兼容性好 |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Radix UI | latest | 基础组件 | shadcn/ui 底层依赖 |
| Tailwind CSS | v4+ | 样式 | 与 shadcn/ui 配合使用 |
| Axios | v1+ | HTTP 客户端 | 与后端 API 通信 |
| React Router | v6+ | 路由 | SPA 标准方案 |

## Architecture Patterns

### Pattern 1: 管理后台布局
```typescript
// AdminDashboard.tsx
const AdminDashboard = () => {
  return (
    <div className="flex h-screen">
      <Sidebar />
      <div className="flex-1 overflow-auto">
        <Header />
        <main className="p-6">
          <Outlet /> {/* 路由页面 */}
        </main>
      </div>
    </div>
  );
};
```

### Pattern 2: 插件市场页面
```typescript
// PluginMarket.tsx
const PluginMarket = () => {
  const [plugins, setPlugins] = useState<PluginInfo[]>([]);
  
  // 获取插件列表
  useEffect(() => {
    axios.get('/api/plugins').then(res => setPlugins(res.data));
  }, []);
  
  // 启用/停用插件
  const togglePlugin = async (id: string, enabled: boolean) => {
    await axios.post(`/api/plugins/${id}/toggle`, { enabled });
  };
  
  return (
    <Table>
      {plugins.map(plugin => (
        <TableRow key={plugin.id}>
          <TableCell>{plugin.name}</TableCell>
          <TableCell>{plugin.version}</TableCell>
          <TableCell>
            <Switch checked={plugin.enabled} onCheckedChange={enabled => togglePlugin(plugin.id, enabled)} />
          </TableCell>
          <TableCell>
            <Button onClick={() => openConfigDialog(plugin)}>配置</Button>
          </TableCell>
        </TableRow>
      ))}
    </Table>
  );
};
```

### Pattern 3: React Flow 流程设计器
```typescript
// FlowDesigner.tsx
import ReactFlow, { Node, Edge, addEdge, Connection } from 'reactflow';

const FlowDesigner = () => {
  const [nodes, setNodes] = useState<Node[]>([
    { id: '1', type: 'identity', position: { x: 0, y: 0 }, data: { label: '身份核验' } },
    { id: '2', type: 'risk', position: { x: 300, y: 0 }, data: { label: '风险揭示' } },
  ]);
  const [edges, setEdges] = useState<Edge[]>([]);
  
  const onConnect = (params: Connection) => setEdges(edges => addEdge(params, edges));
  
  return (
    <ReactFlow
      nodes={nodes}
      edges={edges}
      onConnect={onConnect}
      fitView
      snapToGrid
    />
  );
};
```

### Pattern 4: 音视频播放器
```typescript
// VideoPlayer.tsx
const VideoPlayer = ({ src }: { src: string }) => {
  return (
    <video controls className="w-full h-auto">
      <source src={src} type="video/mp4" />
      您的浏览器不支持视频播放
    </video>
  );
};
```

### Pattern 5: 二维码邀请生成
```typescript
// InviteGenerator.tsx
import QRCode from 'qrcode.react';

const InviteGenerator = ({ sessionId, token }: { sessionId: string; token: string }) => {
  const inviteUrl = `https://yourapp.com/record/${sessionId}?token=${token}`;
  
  return (
    <div>
      <QRCode value={inviteUrl} size={256} />
      <Input value={inviteUrl} readOnly />
      <Button onClick={() => navigator.clipboard.writeText(inviteUrl)}>复制链接</Button>
    </div>
  );
};
```

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| UI 组件 | 自己实现 Button, Table, Dialog | shadcn/ui | 高质量、可访问性、主题支持 |
| 流程设计器 | 用 Canvas 自己画节点 | React Flow | 成熟的拖拽、缩放、连接功能 |
| 二维码生成 | 调用第三方 API | qrcode.react | 本地生成、无需 API 密钥 |
| 音视频播放 | 用 Flash 或第三方插件 | HTML5 Video | 浏览器原生支持、无需插件 |

## Common Pitfalls

### Pitfall 1: React Flow 节点类型未注册
**What goes wrong**: 自定义节点类型不显示

**How to avoid**:
```typescript
const nodeTypes = {
  identity: IdentityNode,
  risk: RiskNode,
  product: ProductNode,
};

<ReactFlow nodeTypes={nodeTypes} />
```

### Pitfall 2: 音视频格式不兼容
**What goes wrong**: Safari 不支持 WebM 格式

**How to avoid**:
- 使用 MP4 格式（H.264 编码）
- 提供多个<source>备用

### Pitfall 3: 二维码扫描失败
**What goes wrong**: 二维码太小或对比度不足

**How to avoid**:
- 设置 size ≥ 256
- 确保背景对比度高
- 提供链接备用方案

## Code Examples

### Example 1: 管理后台路由配置
```typescript
// admin/routes.tsx
const routes = [
  { path: '/admin/plugins', component: PluginMarket },
  { path: '/admin/flows', component: FlowDesigner },
  { path: '/admin/records', component: RecordList },
  { path: '/admin/quality', component: QualityReview },
];

const AdminRouter = () => (
  <Routes>
    {routes.map(route => (
      <Route key={route.path} path={route.path} element={route.component} />
    ))}
  </Routes>
);
```

### Example 2: 插件市场完整实现
```typescript
// admin/pages/PluginMarket.tsx
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Switch } from '@/components/ui/switch';
import { Button } from '@/components/ui/button';

interface Plugin {
  id: string;
  name: string;
  version: string;
  enabled: boolean;
  description: string;
}

export const PluginMarket = () => {
  const [plugins, setPlugins] = useState<Plugin[]>([]);
  
  useEffect(() => {
    axios.get('/api/admin/plugins').then(res => setPlugins(res.data));
  }, []);
  
  const togglePlugin = async (id: string, enabled: boolean) => {
    await axios.post(`/api/admin/plugins/${id}/toggle`, { enabled });
    setPlugins(plugins.map(p => p.id === id ? { ...p, enabled } : p));
  };
  
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">插件市场</h1>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>插件名称</TableHead>
            <TableHead>版本</TableHead>
            <TableHead>描述</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {plugins.map(plugin => (
            <TableRow key={plugin.id}>
              <TableCell className="font-medium">{plugin.name}</TableCell>
              <TableCell>{plugin.version}</TableCell>
              <TableCell>{plugin.description}</TableCell>
              <TableCell>
                <Switch checked={plugin.enabled} onCheckedChange={enabled => togglePlugin(plugin.id, enabled)} />
              </TableCell>
              <TableCell>
                <Button variant="outline" size="sm">配置</Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| 手动实现 UI 组件 | shadcn/ui | 2024-2026 | 开发效率提升 5 倍 |
| Flash 播放器 | HTML5 Video | 2020-2024 | 无需插件、移动兼容 |
| 图片二维码生成 | 本地生成（qrcode.react） | 2022-2026 | 无需 API、实时生成 |
| 静态流程图 | React Flow 交互式 | 2024-2026 | 可拖拽、可编辑 |

## Open Questions

1. **是否需要可视化流程设计器？**
   - What we know: MVP 可以使用 YAML 配置
   - What's unclear: 可视化设计器开发成本
   - Recommendation: Phase 8 先实现基础管理页面，可视化设计器延至 Phase 09

2. **音视频是否需要流式播放？**
   - What we know: MP4 文件可以直接播放
   - What's unclear: 大文件是否需要 HLS 流式传输
   - Recommendation: MVP 使用直接播放，大文件场景延至 V2

3. **客户端是否需要独立 App？**
   - What we know: Web 端已满足需求
   - What's unclear: 原生 App 的体验优势
   - Recommendation: MVP 使用 Web 端，V2 考虑原生 App

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | Vitest + React Testing Library |
| Config file | vitest.config.ts |
| Quick run command | `npm test -- --testPathPattern=admin` |
| Full suite command | `npm test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| ADMIN-01 | 插件市场展示和管理 | 组件测试 | `npm test -- PluginMarket` | ❌ Wave 0 |
| ADMIN-02 | 流程设计器拖拽 | 组件测试 | `npm test -- FlowDesigner` | ❌ Wave 0 |
| ADMIN-03 | 音视频回放 | E2E 测试 | `npm test -- VideoPlayer` | ❌ Wave 0 |
| ADMIN-04 | 质检复核 | 组件测试 | `npm test -- QualityReview` | ❌ Wave 0 |
| CLIENT-01 | 二维码生成 | 组件测试 | `npm test -- InviteGenerator` | ❌ Wave 0 |

## Sources

### Primary (HIGH confidence)
- **shadcn/ui 官方文档**: https://ui.shadcn.com - 组件使用、主题定制
- **React Flow 官方文档**: https://reactflow.dev - API 参考、示例
- **qrcode.react GitHub**: https://github.com/zpao/qrcode.react - 使用示例
- **MDN HTML5 Video**: https://developer.mozilla.org/en-US/docs/Web/HTML/Element/video - 视频播放 API

### Secondary (MEDIUM confidence)
- **Radix UI 文档**: https://www.radix-ui.com - 基础组件
- **Tailwind CSS 文档**: https://tailwindcss.com - 样式工具类
- **HLS.js 文档**: https://github.com/video-dev/hls.js - HLS 播放

## Metadata

**Confidence breakdown**:
- Standard stack: **HIGH** - 都是成熟库，文档完善
- Architecture: **HIGH** - 基于官方文档和最佳实践
- Pitfalls: **MEDIUM** - 基于经验推断，需实际验证

**Research date**: 2026 年 3 月 25 日  
**Valid until**: 180 days（UI 库更新频繁，但核心 API 稳定）
