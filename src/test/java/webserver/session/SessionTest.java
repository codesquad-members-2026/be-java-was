package webserver.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionTest {

    @Test
    @DisplayName("Session is correctly created with the provided ID")
    void createSession() {
        // given
        String expectedId = "test-session-123";
        // when
        Session session = new Session(expectedId);
        // then
        assertThat(session.getId()).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("Attributes are successfully added and retrieved")
    void addAndGetAttribute() {
        // given
        Session session = new Session("test-id");
        String key = "userRole";
        String val = "ADMIN";
        // when
        session.addAttribute(key, val);
        // then
        assertThat(session.getAttribute(key)).isEqualTo(val);
    }

    @Test
    @DisplayName("Retrieving an attribute that hasn't been added returns null")
    void getNonExistentAttribute() {
        // given
        Session session = new Session("test-id");
        // when
        Object result = session.getAttribute("nonExistentKey");
        // then
        assertThat(result).isNull();
    }
}