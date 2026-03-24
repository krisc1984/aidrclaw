package com.aidrclaw.core.session;

import com.aidrclaw.core.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStateMachine {

    private static final Logger logger = LoggerFactory.getLogger(SessionStateMachine.class);

    private final Map<String, SessionState> sessionStates = new ConcurrentHashMap<>();
    private final Map<SessionState, State> stateMap = new ConcurrentHashMap<>();
    private final List<SessionStateChangeListener> listeners = new ArrayList<>();

    public SessionStateMachine() {
        initializeStates();
    }

    private void initializeStates() {
        stateMap.put(SessionState.IDLE, new IdleState());
        stateMap.put(SessionState.INITIALIZING, new InitializingState());
        stateMap.put(SessionState.RECORDING, new RecordingState());
        stateMap.put(SessionState.INSPECTING, new InspectingState());
        stateMap.put(SessionState.COMPLETED, new CompletedState());
        stateMap.put(SessionState.ERROR, new ErrorState());
    }

    public void createSession(String sessionId) {
        sessionStates.put(sessionId, SessionState.IDLE);
        logger.info("创建会话：{}", sessionId);
        notifyStateChange(sessionId, null, SessionState.IDLE);
    }

    public void startRecording(String sessionId) {
        transition(sessionId, SessionState.INITIALIZING, () -> {
            transition(sessionId, SessionState.RECORDING, null);
        });
    }

    public void stopRecording(String sessionId) {
        transition(sessionId, SessionState.INSPECTING, null);
    }

    public void startInspection(String sessionId) {
        transition(sessionId, SessionState.INSPECTING, null);
    }

    public void completeSession(String sessionId, boolean success) {
        transition(sessionId, success ? SessionState.COMPLETED : SessionState.ERROR, null);
    }

    public void error(String sessionId, String errorCode, String errorMessage) {
        transition(sessionId, SessionState.ERROR, null);
    }

    private void transition(String sessionId, SessionState newState, Runnable callback) {
        SessionState currentState = sessionStates.get(sessionId);
        if (currentState == null) {
            logger.warn("会话不存在：{}", sessionId);
            return;
        }

        if (currentState == newState) {
            return;
        }

        State state = stateMap.get(currentState);
        if (state != null) {
            SessionContext context = new SessionContext(sessionId, newState);
            state.onExit(context);
        }

        sessionStates.put(sessionId, newState);

        State nextState = stateMap.get(newState);
        if (nextState != null) {
            SessionContext context = new SessionContext(sessionId, newState);
            nextState.onEnter(context);
        }

        logger.info("会话状态转换：{} -> {} -> {}", sessionId, currentState, newState);
        notifyStateChange(sessionId, currentState, newState);

        if (callback != null) {
            callback.run();
        }
    }

    public SessionState getState(String sessionId) {
        return sessionStates.get(sessionId);
    }

    public void addStateChangeListener(SessionStateChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyStateChange(String sessionId, SessionState oldState, SessionState newState) {
        for (SessionStateChangeListener listener : listeners) {
            try {
                listener.onStateChange(sessionId, oldState, newState);
            } catch (Exception e) {
                logger.error("状态变更监听器异常", e);
            }
        }
    }

    public interface SessionStateChangeListener {
        void onStateChange(String sessionId, SessionState oldState, SessionState newState);
    }
}
