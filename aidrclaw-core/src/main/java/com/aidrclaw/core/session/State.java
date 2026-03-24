package com.aidrclaw.core.session;

public interface State {
    void onEnter(SessionContext context);
    void onExit(SessionContext context);
    State onEvent(SessionEvent event, SessionContext context);
}
