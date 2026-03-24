package com.aidrclaw.core.event;

public class StartInspectionEvent extends DomainEvent {
    private final String sessionId;

    public StartInspectionEvent(Object source, String sessionId) {
        super(source);
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
