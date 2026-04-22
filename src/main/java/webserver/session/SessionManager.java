package webserver.session;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

public class SessionManager {
    public static final String SESSION_COOKIE_NAME = "sessionId";
    private static final String COOKIE_PATH = "/";
    private static final int SESSION_TIMEOUT_SECONDS = 7200;

    private final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    public Session get(HttpRequest request, HttpResponse response) {
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] pair = cookie.trim().split("=", 2);
                if (pair.length == 2 && pair[0].equals(SESSION_COOKIE_NAME)) {
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
        response.addCookie(new Cookie(SESSION_COOKIE_NAME, sessionId, COOKIE_PATH, SESSION_TIMEOUT_SECONDS));
        return session;
    }
}
