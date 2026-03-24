package com.aidrclaw.core.session;

public class IdleState implements State {
    @Override
    public void onEnter(SessionContext context) {
    }

    @Override
    public void onExit(SessionContext context) {
    }

    @Override
    public State onEvent(SessionEvent event, SessionContext context) {
        return this;
    }
}
