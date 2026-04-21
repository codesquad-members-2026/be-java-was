package webserver.session;

import java.util.HashMap;
import java.util.Map;

public class Session {
    private final Map<String, Object> sessionStore;
    private final String id;

    public Session(String id) {
        this.sessionStore = new HashMap<>();
        this.id = id;
    }

    public Object get(String key) {
        return sessionStore.get(key);
    }

    public String getId() {
        return id;
    }

    public void addAttribute(String key, Object value) {
        sessionStore.put(key, value);
    }
}
