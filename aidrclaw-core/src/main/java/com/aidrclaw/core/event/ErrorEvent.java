package com.aidrclaw.core.event;

public class ErrorEvent extends DomainEvent {
    private final String sessionId;
    private final String errorCode;
    private final String errorMessage;

    public ErrorEvent(Object source, String sessionId, String errorCode, String errorMessage) {
        super(source);
        this.sessionId = sessionId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
