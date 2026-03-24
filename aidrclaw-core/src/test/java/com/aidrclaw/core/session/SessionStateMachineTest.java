package com.aidrclaw.core.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionStateMachineTest {

    private SessionStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new SessionStateMachine();
    }

    @Test
    void createSession_shouldSetInitialState() {
        stateMachine.createSession("test-session-1");
        
        assertEquals(SessionState.IDLE, stateMachine.getState("test-session-1"));
    }

    @Test
    void startRecording_shouldTransitionToRecording() {
        stateMachine.createSession("test-session-2");
        stateMachine.startRecording("test-session-2");
        
        assertEquals(SessionState.RECORDING, stateMachine.getState("test-session-2"));
    }

    @Test
    void stopRecording_shouldTransitionToInspecting() {
        stateMachine.createSession("test-session-3");
        stateMachine.startRecording("test-session-3");
        stateMachine.stopRecording("test-session-3");
        
        assertEquals(SessionState.INSPECTING, stateMachine.getState("test-session-3"));
    }

    @Test
    void completeSession_shouldTransitionToCompleted() {
        stateMachine.createSession("test-session-4");
        stateMachine.startRecording("test-session-4");
        stateMachine.stopRecording("test-session-4");
        stateMachine.startInspection("test-session-4");
        stateMachine.completeSession("test-session-4", true);
        
        assertEquals(SessionState.COMPLETED, stateMachine.getState("test-session-4"));
    }

    @Test
    void error_shouldTransitionToError() {
        stateMachine.createSession("test-session-5");
        stateMachine.error("test-session-5", "TEST_ERROR", "Test error message");
        
        assertEquals(SessionState.ERROR, stateMachine.getState("test-session-5"));
    }
}
