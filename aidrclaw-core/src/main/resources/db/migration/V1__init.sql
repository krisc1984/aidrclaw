-- V1__init.sql - 初始化双录系统数据库表结构

-- 双录会话表
CREATE TABLE flow_session (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL UNIQUE,
    state VARCHAR(32) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    result VARCHAR(32),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_flow_session_session_id ON flow_session(session_id);
CREATE INDEX idx_flow_session_state ON flow_session(state);
CREATE INDEX idx_flow_session_created_at ON flow_session(created_at);

-- 会话状态历史表
CREATE TABLE flow_state_history (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    from_state VARCHAR(32) NOT NULL,
    to_state VARCHAR(32) NOT NULL,
    event VARCHAR(64),
    transition_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_flow_state_history_session_id ON flow_state_history(session_id);
CREATE INDEX idx_flow_state_history_transition_time ON flow_state_history(transition_time);

-- 文件存储表
CREATE TABLE storage_file (
    id BIGSERIAL PRIMARY KEY,
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    mime_type VARCHAR(128),
    business_type VARCHAR(32) NOT NULL,
    business_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_storage_file_business ON storage_file(business_type, business_id);
CREATE INDEX idx_storage_file_hash ON storage_file(file_hash);

-- 插件实例表
CREATE TABLE plugin_instance (
    id BIGSERIAL PRIMARY KEY,
    plugin_id VARCHAR(64) NOT NULL UNIQUE,
    plugin_name VARCHAR(128) NOT NULL,
    version VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    loaded_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_plugin_instance_plugin_id ON plugin_instance(plugin_id);
CREATE INDEX idx_plugin_instance_status ON plugin_instance(status);
