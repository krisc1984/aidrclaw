package com.aidrclaw.core.session;

import com.aidrclaw.core.event.DomainEvent;

public abstract class SessionEvent extends DomainEvent {
    private final String sessionId;

    public SessionEvent(Object source, String sessionId) {
        super(source);
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
