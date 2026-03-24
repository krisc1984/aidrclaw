# Phase 3: 存储插件域 - Context

**Gathered:** 2026 年 3 月 24 日
**Status:** Ready for planning

<domain>
## Phase Boundary

**交付目标**: 实现音视频文件存储和加密归档能力

**本阶段包括**:
- 对象存储插件：支持 MinIO 对象存储（S3 兼容协议）
- 加密归档插件：AES-256-GCM 加密存储
- 元数据管理：StorageFile 表扩展，支持加密状态、存储位置
- 存储策略：支持配置选择存储后端（本地/MinIO）

**本阶段不包括**:
- 音视频转码/压缩 — V1.5 考虑
- CDN 分发 — V2.0 考虑
- 冷数据归档到磁带库 — V2.0 考虑
- 多区域复制 — V2.0 考虑

</domain>

<decisions>
## Implementation Decisions

### 存储后端选型

**决策**: 本地存储 + MinIO 双插件架构

**理由**:
- Phase 1 已交付 LocalStoragePlugin，可复用
- MinIO 是开源 S3 兼容对象存储，适合私有化部署
- 金融企业可能已有 MinIO 集群，避免重复建设
- 通过插件化设计，用户可选择或同时使用两种存储

**技术选型**:
- **MinIO Java SDK** — 官方客户端，支持 S3 API
- **存储策略接口** — 定义 `StorageStrategy` 接口，支持动态切换
- **配置文件指定默认存储** — `storage.default-backend: local|minio`

### 加密方案

**决策**: AES-256-GCM 加密算法

**理由**:
- GCM 模式提供认证加密（AEAD），防止篡改
- AES-256 是金融行业标准加密算法
- 硬件加速支持（AES-NI），性能优秀
- Java 原生支持（`javax.crypto`）

**密钥管理**:
- **密钥存储** — 使用 JCEKS Keystore 存储主密钥
- **密钥派生** — 使用 PBKDF2 从密码派生加密密钥
- **每文件随机 IV** — 每次加密使用随机 IV，保证安全性
- **密钥轮换** — V1.5 支持，V1.0 固定主密钥

**加密流程**:
1. 生成随机 256 位 AES 密钥（或使用主密钥派生）
2. 生成随机 96 位 IV
3. 使用 AES-GCM 加密文件
4. 将 IV 和加密标签（tag）附加到密文
5. 元数据记录加密状态和密钥 ID

### 元数据表结构

**决策**: 扩展现有 StorageFile 表

**现有字段** (Phase 1):
```sql
CREATE TABLE storage_file (
    id BIGSERIAL PRIMARY KEY,
    file_path VARCHAR(512),      -- 文件路径或对象键
    file_size BIGINT,            -- 文件大小 (bytes)
    file_hash VARCHAR(64),       -- SHA-256 哈希
    mime_type VARCHAR(128),      -- MIME 类型
    business_type VARCHAR(64),   -- 业务类型
    business_id VARCHAR(128),    -- 业务 ID
    created_at TIMESTAMP         -- 创建时间
);
```

**新增字段** (Phase 3):
```sql
ALTER TABLE storage_file ADD COLUMN (
    storage_backend VARCHAR(32),    -- 存储后端：local|minio
    bucket_name VARCHAR(128),       -- MinIO bucket 名称
    encrypted BOOLEAN DEFAULT FALSE, -- 是否加密
    encryption_key_id VARCHAR(64),  -- 加密密钥 ID
    encrypted_iv VARCHAR(256),      -- 加密 IV (base64)
    compression VARCHAR(32),        -- 压缩算法：none|gzip
    retention_days INTEGER,         -- 保留天数
    archived_at TIMESTAMP,          -- 归档时间
    accessed_at TIMESTAMP           -- 最后访问时间
);
```

### 插件设计

**加密归档插件** (`EncryptionArchivePlugin`):
- **输入**: 文件流、业务元数据
- **处理**: 
  1. 计算 SHA-256 哈希
  2. AES-GCM 加密
  3. 调用存储插件保存
  4. 元数据入库
- **输出**: fileId, filePath, encryptionMetadata

**MinIO 存储插件** (`MinioStoragePlugin`):
- **配置**: endpoint, accessKey, secretKey, bucket, region
- **操作**: putObject, getObject, deleteObject, presignedUrl
- **元数据**: 对象标签（businessType, businessId）

**存储策略插件** (`StorageStrategyPlugin`):
- **路由规则**: 根据文件大小、业务类型选择存储后端
- **示例**: 
  - 文件 < 100MB → 本地存储
  - 文件 ≥ 100MB → MinIO
  - 业务类型 = "temporary" → 本地存储（临时文件）

### 与 Phase 1/2 的接口

**Phase 1 复用**:
- `Plugin` 接口 — 所有存储插件实现此接口
- `PluginContext` / `PluginResult` — 参数和返回值
- `StorageFile` 实体 — 扩展字段
- `StorageFileMapper` — MyBatis Mapper

**Phase 2 集成**:
- **Web 采集插件** → 调用加密归档插件保存音视频
- **移动端采集插件** → 调用加密归档插件保存音视频
- **接口调用**:
  ```java
  PluginContext context = new PluginContext();
  context.getInput().put("action", "saveEncrypted");
  context.getInput().put("data", inputStream);
  context.getInput().put("businessType", "dual-recording");
  context.getInput().put("businessId", sessionId);
  
  PluginResult result = encryptionPlugin.execute(context);
  ```

</decisions>

<code_context>
## Existing Code Insights

### 可复用资产

**LocalStoragePlugin** (`aidrclaw-plugins-storage/LocalStoragePlugin.java`):
- 已实现 `save`, `load`, `delete` 操作
- 使用 SHA-256 哈希作为文件名
- 元数据映射到 `StorageFile` 实体
- 可作为 MinIO 插件的实现参考

**StorageFile 实体** (`aidrclaw-core/entity/StorageFile.java`):
- 包含基础字段：filePath, fileSize, fileHash, mimeType 等
- 需要扩展添加加密、存储后端字段

**应用配置** (`application.yml`):
- 已有 PostgreSQL 数据源配置
- 需要添加 MinIO 配置段：
  ```yaml
  minio:
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: dual-recording
    region: us-east-1
  ```

### 既定模式

**插件模式**:
- 实现 `Plugin` 接口：`init()`, `execute()`, `destroy()`, `getMetadata()`
- 使用 `@Component` 注解，Spring 管理生命周期
- 配置通过 `PluginContext` 传入

**数据库访问**:
- MyBatis XML 映射位于 `classpath:mappers/*.xml`
- 使用 `@Mapper` 注解接口
- 支持动态 SQL

**错误处理**:
- 使用 `PluginResult.error(statusCode, message)` 返回错误
- 日志使用 SLF4J

### 集成点

**Spring Boot 集成**:
- 使用 `@Autowired` 注入 Mapper
- 使用 `@ConfigurationProperties` 绑定配置

**加密库**:
- Java 原生 `javax.crypto.Cipher` (AES-GCM)
- Guava `Hashing.sha256()` (已有依赖)

**MinIO SDK**:
- Maven 依赖：`io.minio:minio:8.5.x`
- 使用 `MinioClient` 进行对象操作

</code_context>

<specifics>
## Specific Ideas

**用户偏好**:
- 简单优先：V1.0 仅支持 AES-256-GCM，不支持密钥轮换
- 用户体验：加密对用户透明，无需手动操作
- 技术栈：Java 原生加密库，MinIO 官方 SDK

**技术约束**:
- 纯私有化部署：MinIO 必须部署在私有环境
- 数据不出域：加密密钥不得离开企业内网
- 合规安全：加密算法必须符合金融监管要求

**性能目标**:
- 加密延迟：< 100ms/MB
- 单节点吞吐：≥ 100MB/s
- 并发支持：100+ 并发加密操作

**安全要求**:
- 密钥存储：使用 JCEKS Keystore，密码保护环境变量
- IV 唯一性：每次加密使用随机 IV
- 哈希校验：加密前后计算哈希，确保完整性

**Deferred Ideas** (留到后续阶段):
- 密钥轮换 — V1.5 支持
- 压缩存储 — V1.5 支持 gzip 压缩
- 多区域复制 — V2.0 支持
- CDN 分发 — V2.0 支持
- 冷数据归档 — V2.0 支持磁带库归档

</specifics>

<deferred>
## Deferred Ideas

- **密钥轮换** — 定期更换主密钥，重新加密旧数据
- **压缩存储** — 使用 gzip/zstd 压缩后加密
- **多区域复制** — MinIO 跨数据中心复制
- **CDN 分发** — 视频回放使用 CDN 加速
- **冷数据归档** — 归档到低成本存储（磁带库、Glacier）
- **客户端加密** — 在浏览器/移动端加密后上传
- **HSM 集成** — 硬件安全模块存储密钥
- **审计日志** — 记录所有加密/解密操作

</deferred>

---

*Phase: 03-storage-plugins*
*Context gathered: 2026-03-24*
