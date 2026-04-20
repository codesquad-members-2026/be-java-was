package session;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    // ConcurrentHashMap은 검색 속도가 O(1)에 해당하고, 읽기에 락을 걸지 않기 때문에 세션 조회에 의한 병목은 발생하지 않는다 -> 값을 새로 쓸 때만 부분적 락이 걸림
    // TODO: Redis나 JWT로 대체
    //  1. 서버가 여러개일 경우 로드밸런스에 의해 쿠키 정보가 없는 서버로 요청을 보낼 경우 세션 정보를 불러오지 못할 수 있음
    //  2. OOM의 위험 존재
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
