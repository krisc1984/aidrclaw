package com.aidrclaw.core.session;

/**
 * 双录会话状态枚举
 */
public enum SessionState {
    IDLE("空闲"),
    INITIALIZING("初始化中"),
    RECORDING("录制中"),
    INSPECTING("质检中"),
    COMPLETED("已完成"),
    ERROR("错误");

    private final String description;

    SessionState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
