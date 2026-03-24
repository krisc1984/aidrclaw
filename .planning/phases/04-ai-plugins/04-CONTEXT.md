# Phase 4: AI 插件域基础 - Context

**Gathered:** 2026 年 3 月 24 日
**Status:** Ready for planning

<domain>
## Phase Boundary

**交付目标**: 实现基础 AI 能力插件（ASR、TTS、人脸比对、活体检测）

**本阶段包括**:
- ASR 插件：语音识别，将客户语音转文本
- TTS 插件：语音合成，为虚拟坐席生成语音
- 人脸比对插件：核验客户身份（客户人脸 vs 身份证照片）
- 活体检测插件：动作检测（眨眼、张嘴）或静默活体

**本阶段不包括**:
- 合规检测（违规词汇、动作检测）— Phase 7
- 虚拟坐席 Agent 对话逻辑 — Phase 6
- 流程编排引擎 — Phase 5
- 管理后台 UI — Phase 8

</domain>

<decisions>
## Implementation Decisions

### AI 能力选型

**决策**: 优先集成开源模型，保留云服务接口

**理由**:
- 金融企业私有化部署要求数据不出域
- 开源模型可本地部署，满足合规要求
- 保留云服务接口便于未来扩展

**技术选型**:
- **ASR**: FunASR (阿里开源) 或 Whisper (OpenAI)
  - FunASR 优势：中文识别率高，支持流式识别
  -  Whisper 优势：多语言支持好
- **TTS**: VITS 或 Edge-TTS
  - VITS: 开源高质量 TTS
  - Edge-TTS: 微软免费服务（需评估私有化）
- **人脸比对**: InsightFace 或 Face Recognition
  - InsightFace: 准确率高≥99%，支持活体检测
- **活体检测**: 基于 InsightFace 的动作检测

### 部署模式

**决策**: 本地部署为主，云服务为备选

**部署架构**:
```
┌─────────────────┐
│  AI Plugin API  │
├─────────────────┤
│  Local Engine   │ ← FunASR / InsightFace
├─────────────────┤
│  Cloud Fallback │ ← 阿里云/讯飞 (可选)
└─────────────────┘
```

**配置切换**:
```yaml
ai:
  asr:
    provider: local  # local | cloud
    local:
      model: funasr
    cloud:
      provider: aliyun
      api-key: ${ALYUN_ASR_KEY}
```

### 插件接口设计

**决策**: 统一 AI Plugin 接口，支持同步/异步调用

**接口规范**:
```java
public interface AIPlugin extends Plugin {
    // 同步调用（小数据量）
    AIResult processSync(AIRequest request);
    
    // 异步调用（大数据量，如长音频）
    String processAsync(AIRequest request);
    AIResult getResult(String taskId);
}
```

**输入输出**:
- ASR: `byte[] audio` → `String text + confidence`
- TTS: `String text` → `byte[] audio`
- 人脸比对：`byte[] image1, byte[] image2` → `double similarity + boolean match`
- 活体检测：`byte[] image + List<String> actions` → `boolean passed + List<String> detectedActions`

### 性能目标

**决策**: 设定明确的性能指标

**指标**:
- ASR: 识别延迟 < 1 秒（10 秒音频），准确率≥95%
- TTS: 合成延迟 < 500ms（100 字），MOS≥4.0
- 人脸比对：比对时间 < 200ms，准确率≥99%
- 活体检测：检测时间 < 500ms，准确率≥98%

### 与 Phase 2/3 集成

**Phase 2 采集插件集成**:
- ASR 插件接收采集插件的音频流
- 人脸比对接收采集插件的视频帧

**Phase 3 存储插件集成**:
- AI 处理结果（文本、比对结果）元数据入库
- 音频/视频原始文件已加密存储

**接口调用**:
```java
// ASR 处理录制音频
PluginContext asrContext = new PluginContext();
asrContext.getInput().put("action", "recognize");
asrContext.getInput().put("audioData", audioBytes);
PluginResult asrResult = asrPlugin.execute(asrContext);
String recognizedText = asrResult.getData("text", String.class);

// 人脸比对
PluginContext faceContext = new PluginContext();
faceContext.getInput().put("action", "compare");
faceContext.getInput().put("image1", customerFaceBytes);
faceContext.getInput().put("image2", idCardFaceBytes);
PluginResult faceResult = facePlugin.execute(faceContext);
```

</decisions>

<code_context>
## Existing Code Insights

### 可复用资产

**Phase 1 交付**:
- `Plugin` 接口：所有 AI 插件需实现此接口
- `PluginContext` / `PluginResult`: 统一参数和返回值
- `PluginManager`: 插件加载和管理

**Phase 2 交付**:
- `WebCapturePlugin`: 可获取音频流用于 ASR
- `CameraPreview`: 可捕获视频帧用于人脸比对

**Phase 3 交付**:
- `EncryptionArchivePlugin`: AI 处理结果可加密存储
- `StorageFile` 实体：扩展支持 AI 元数据

### 既定模式

**插件模式**:
- 实现 `Plugin` 接口：`init()`, `execute()`, `destroy()`, `getMetadata()`
- 使用 `@Component` 注解，Spring 管理生命周期
- 配置通过 `PluginContext` 传入

**错误处理**:
- 使用 `PluginResult.error(statusCode, message)` 返回错误
- 日志使用 SLF4J

### 集成点

**Spring Boot 集成**:
- 使用 `@Autowired` 注入其他插件
- 使用 `@ConfigurationProperties` 绑定 AI 配置

**AI 库依赖**:
- FunASR: Maven 坐标待确认
- InsightFace: Python 库，需通过 JNI 或 HTTP 调用
- 或考虑 Java 原生实现

</code_context>

<specifics>
## Specific Ideas

**用户偏好**:
- 简单优先：优先集成成熟开源方案，不重复造轮子
- 用户体验：低延迟、高准确率
- 技术栈：Java 优先，Python 模型需封装

**技术约束**:
- 纯私有化部署：AI 模型必须支持本地推理
- 数据不出域：音频/视频/人脸数据不得上传公有云
- 性能要求：满足实时交互需求

**性能目标**:
- ASR 延迟：< 1 秒（10 秒音频）
- TTS 延迟：< 500ms（100 字）
- 人脸比对：< 200ms
- 活体检测：< 500ms

**Deferred Ideas** (留到后续阶段):
- 情绪检测 — V1.5 增加
- 复杂意图理解 — V1.5 引入大模型
- 实时质检 — Phase 7
- 虚拟坐席对话逻辑 — Phase 6

</specifics>

<deferred>
## Deferred Ideas

- **情绪检测**: 分析客户情绪状态（V1.5）
- **大模型意图理解**: 基于 LLM 的对话理解（V1.5）
- **实时质检**: 录制过程中实时检测（Phase 7）
- **虚拟坐席对话逻辑**: 基于规则/意图的对话流程（Phase 6）
- **多模态融合**: 语音 + 视觉 + 文本联合分析（V2.0）
- **自学习模型**: 根据业务数据持续优化（V2.0）

</deferred>

---

*Phase: 04-ai-plugins*
*Context gathered: 2026 年 3 月 24 日*
