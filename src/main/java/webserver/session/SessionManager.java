package webserver.session;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

}
