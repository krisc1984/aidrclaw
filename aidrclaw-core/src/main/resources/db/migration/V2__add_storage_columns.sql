-- Phase 3: 存储插件域 - 扩展 StorageFile 表
-- 添加存储后端、加密、归档相关字段

ALTER TABLE storage_file 
ADD COLUMN storage_backend VARCHAR(32) DEFAULT 'local' COMMENT '存储后端：local|minio',
ADD COLUMN bucket_name VARCHAR(128) COMMENT 'MinIO bucket 名称',
ADD COLUMN encrypted BOOLEAN DEFAULT FALSE COMMENT '是否加密',
ADD COLUMN encryption_key_id VARCHAR(64) COMMENT '加密密钥 ID',
ADD COLUMN encrypted_iv VARCHAR(256) COMMENT '加密 IV (base64)',
ADD COLUMN compression VARCHAR(32) DEFAULT 'none' COMMENT '压缩算法：none|gzip',
ADD COLUMN retention_days INTEGER COMMENT '保留天数',
ADD COLUMN archived_at TIMESTAMP COMMENT '归档时间',
ADD COLUMN accessed_at TIMESTAMP COMMENT '最后访问时间';

-- 添加索引
CREATE INDEX idx_storage_backend ON storage_file(storage_backend);
CREATE INDEX idx_encrypted ON storage_file(encrypted);
CREATE INDEX idx_archived_at ON storage_file(archived_at);
