package com.aidrclaw.core.event;

public class StopRecordingEvent extends DomainEvent {
    private final String sessionId;

    public StopRecordingEvent(Object source, String sessionId) {
        super(source);
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
