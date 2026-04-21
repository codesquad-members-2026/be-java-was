package webserver.session;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import webserver.http.HttpRequest;

public class SessionManager {
    private ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    public Session get(HttpRequest request) {
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] pair = cookie.trim().split("=", 2);
                if (pair.length == 2 && pair[0].equals("sessionId")) {
                    Session session = sessionMap.get(pair[1]);
                    if (session != null) {
                        return session;
                    }
                }
            }
        }
        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId);
        sessionMap.put(sessionId, session);
        return session;
    }
}
