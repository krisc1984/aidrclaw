package com.aidrclaw.core.entity;

import java.time.LocalDateTime;

public class FlowStateHistory {
    private Long id;
    private String sessionId;
    private String fromState;
    private String toState;
    private String event;
    private LocalDateTime transitionTime;
    private LocalDateTime createdAt;

    public FlowStateHistory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFromState() {
        return fromState;
    }

    public void setFromState(String fromState) {
        this.fromState = fromState;
    }

    public String getToState() {
        return toState;
    }

    public void setToState(String toState) {
        this.toState = toState;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public LocalDateTime getTransitionTime() {
        return transitionTime;
    }

    public void setTransitionTime(LocalDateTime transitionTime) {
        this.transitionTime = transitionTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
