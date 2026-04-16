package webserver.session;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    public static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public static void addSession(String sessionId, Session session){
        sessions.put(sessionId, session);
    }

    public static boolean isValid(String sessionId){
        Session session = sessions.get(sessionId);

        if(session == null)
            return false;

        if(session.isExpired()){
            sessions.remove(sessionId);
            return false;
        }

        session.access();
        return true;
    }

    public static void removeSession(String sessionId){
        sessions.remove(sessionId);
    }
}
