package com.aidrclaw.core.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionStateTest {

    @Test
    void states_shouldHaveDescriptions() {
        assertNotNull(SessionState.IDLE.getDescription());
        assertNotNull(SessionState.RECORDING.getDescription());
        assertNotNull(SessionState.INSPECTING.getDescription());
        assertNotNull(SessionState.COMPLETED.getDescription());
        assertNotNull(SessionState.ERROR.getDescription());
    }
}
