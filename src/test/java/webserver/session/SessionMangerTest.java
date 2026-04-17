package webserver.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        // Initialize a fresh SessionManager before each test
        sessionManager = new SessionManager();
    }

    @Test
    @DisplayName("createNewSession creates a valid session, generates a UUID, and stores it")
    void createNewSession() {
        // when
        Session session = sessionManager.createNewSession();
        // then
        assertThat(session).isNotNull();
        assertThat(session.getId()).isNotBlank(); // Checks UUID generation isn't empty
        // Verify it was actually saved in the map
        assertThat(sessionManager.getSession(session.getId())).isEqualTo(session);
    }

    @Test
    @DisplayName("getSession returns the correct session when given a valid ID")
    void getSession() {
        // given
        Session createdSession = sessionManager.createNewSession();
        String validId = createdSession.getId();
        // when
        Session retrievedSession = sessionManager.getSession(validId);
        // then
        assertThat(retrievedSession).isNotNull();
        assertThat(retrievedSession).isEqualTo(createdSession); // Checks object reference equality
    }

    @Test
    @DisplayName("getSession returns null when querying an unknown ID")
    void getSession_NotFound() {
        // when
        Session retrievedSession = sessionManager.getSession("random-invalid-uuid-123");
        // then
        assertThat(retrievedSession).isNull();
    }

    @Test
    @DisplayName("removeSession returns the removed session and successfully deletes it from the manager")
    void removeSession() {
        // given
        Session createdSession = sessionManager.createNewSession();
        String sessionId = createdSession.getId();
        // when
        Session removedSession = sessionManager.removeSession(sessionId);
        // then
        assertThat(removedSession).isEqualTo(createdSession); // Should return the removed object
        assertThat(sessionManager.getSession(sessionId)).isNull(); // Map should no longer hold this ID
    }
}