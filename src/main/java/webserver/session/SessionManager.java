package webserver.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class SessionManager {
    private Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    public Session createNewSession(){
        String newID = UUID.randomUUID().toString();
        Session newSession = new Session(newID);
        this.sessionMap.put(newID, newSession);
        return newSession;
    }

    public Session removeSession(String sessionID){
        return sessionMap.remove(sessionID);
    }

    public Session getSession(String sessionID){
        return this.sessionMap.get(sessionID);
    }
}
