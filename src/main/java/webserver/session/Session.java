package webserver.session;

import java.util.HashMap;
import java.util.Map;

public class Session {
    private final Map<String, Object> sessionStore;
    private final String sessionId;

    public Session(String sessionId) {
        this.sessionStore = new HashMap<>();
        this.sessionId = sessionId;
    }

    public Object get(String key) {
        return sessionStore.get(key);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void put(String key, Object value) {
        sessionStore.put(key, value);
    }
}
