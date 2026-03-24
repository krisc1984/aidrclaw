package com.aidrclaw.core.event;

/**
 * 开始录制事件
 */
public class StartRecordingEvent extends DomainEvent {

    private final String sessionId;

    public StartRecordingEvent(Object source, String sessionId) {
        super(source);
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
