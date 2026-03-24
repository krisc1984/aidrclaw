package com.aidrclaw.core.event;

public class CompleteEvent extends DomainEvent {
    private final String sessionId;
    private final boolean success;

    public CompleteEvent(Object source, String sessionId, boolean success) {
        super(source);
        this.sessionId = sessionId;
        this.success = success;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isSuccess() {
        return success;
    }
}
