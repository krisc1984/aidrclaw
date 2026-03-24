# Phase 2: 采集插件域 - Context

**Gathered:** 2026-03-24
**Status:** Ready for planning

<domain>
## Phase Boundary

**实现 Web 和移动端音视频采集能力**

本阶段交付：
- Web 采集插件：基于 WebRTC 的浏览器端音视频采集
- 移动端采集插件：React Native 跨平台 App，支持 iOS/Android
- 设备管理：摄像头/麦克风选择和切换

本阶段**不**包括：
- AI 处理（ASR、人脸比对）— Phase 4
- 存储/加密 — Phase 3
- 流程编排 — Phase 5
- 管理后台/客户端 UI — Phase 8

</domain>

<decisions>
## Implementation Decisions

### Web 采集方案
- ✅ **纯前端实现** — 无需后端信令服务，适合一对一录制场景
- ✅ **原生 WebRTC API** — 使用 `navigator.mediaDevices.getUserMedia()`，无额外依赖
- ✅ **固定分辨率** — 720p (1280x720) @ 30fps
- ✅ **简单错误提示** — 失败时显示友好提示，用户自行解决权限问题

### 移动端方案
- ✅ **React Native 跨平台** — 一套代码支持 iOS/Android，使用 `react-native-webrtc` 库
- ✅ **后台行为** — App 切到后台时暂停录制，返回时继续
- ✅ **设备切换** — UI 按钮切换前置/后置摄像头，静音/取消静音按钮
- ✅ **权限请求** — 混合方式：启动时说明用途，录制前再次确认（使用 React Native Permissions 库）

### 音视频质量
- ✅ **视频规格** — 720p (1280x720) @ 30fps，固定码率 1.5 Mbps
- ✅ **音频规格** — 标准音质 44.1kHz 单声道，~64kbps
- ✅ **设备切换** — 不支持录制中切换设备，开始录制前选择

### 设备管理
- ✅ **设备选择** — 自动选择系统默认摄像头/麦克风
- ✅ **设备断开** — 检测到设备断开时暂停录制，显示提示，需重新连接后继续
- ✅ **预览** — 实时预览摄像头画面，用户可确认效果
- ✅ **错误处理** — 友好错误提示，包含解决建议

### Claude's Discretion
以下领域由 Claude 自行决定实现方式：
- 具体代码结构和组件组织
- 错误文案的具体措辞
- UI 组件库选择（如使用 shadcn/ui、Ant Design Mobile 等）
- 构建工具配置（Vite、Webpack 等）

</decisions>

<code_context>
## Existing Code Insights

### Reusable Asset
- **Plugin API 接口** — `aidrclaw-plugin-api` 模块已定义 `Plugin` 接口，采集插件需实现此接口
- **PluginLoader** — `aidrclaw-core` 中的 `PluginLoader` 可用于发现和加载采集插件
- **LocalStoragePlugin** — 可作为插件实现的参考模式

### Established Patterns
- **Spring Boot 3.x** — 项目基于 Spring Boot 3.2.4，Java 17
- **Maven 多模块** — 父 POM + 子模块结构
- **MyBatis** — 数据库访问使用 MyBatis
- **SPI 机制** — 插件发现使用 Java SPI (`META-INF/services`)

### Integration Points
- **插件注册** — 采集插件需要在 `META-INF/services/com.aidrclaw.plugin.Plugin` 中注册
- **配置注入** — 使用 Spring `@ConfigurationProperties` 注入采集参数
- **事件总线** — 可使用 Spring `ApplicationEventPublisher` 发布采集事件（如开始、暂停、停止）

</code_context>

<specifics>
## Specific Ideas

**用户偏好**:
- 简单优先：固定分辨率、固定码率、自动选择设备
- 用户体验：实时预览、友好错误提示、混合权限请求
- 技术栈：原生 WebRTC API（无依赖）、React Native 跨平台

**技术约束**:
- 纯私有化部署：所有组件必须支持本地部署
- 数据不出域：音视频数据不得上传到公有云
- 带宽假设：1.5 Mbps 视频码率适用于 WiFi 或良好 4G 环境

**Deferred Ideas** (留到后续阶段):
- 动态码率调整 — 可留到 V1.5 根据网络状况优化
- 屏幕录制插件 — V1.5 增加
- 多人会议支持 — 需要信令服务，V2.0 考虑
- 后台继续录制 — 需要后台服务，V1.5 考虑

</specifics>

<deferred>
## Deferred Ideas

- **动态码率调整** — 根据网络状况自动调整 (500kbps - 3 Mbps)
- **后台继续录制** — 使用后台服务保活
- **屏幕录制插件** — V1.5 增加
- **多人会议支持** — 需要信令服务器
- **1080p 高清录制** — 带宽需求高，V1.5 考虑
- **立体声音频** — 对话场景无明显优势

</deferred>

---

*Phase: 02-capture-plugins*
*Context gathered: 2026-03-24*
