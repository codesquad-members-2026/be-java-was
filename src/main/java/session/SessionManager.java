package session;

import model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<String, User> sessions = new ConcurrentHashMap<String, User>();
    private static final SessionManager instance = new SessionManager();

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, user);
        return sessionId;
    }

    public User read(String sessionId) {
        // TODO: 만약 존재하지 않거나 만료된 세션 ID를 조회했을 때의 예외 처리나 반환값에 대해 고민해 보세요.
        return sessions.get(sessionId);
    }

    public void delete(String sessionId) {
        sessions.remove(sessionId);
    }

    // TODO (심화): 오래된 세션을 자동으로 정리(Clean up)하는 로직이 필요할까요?
    // 세션의 '생성 시간'을 기록하고 일정 시간이 지나면 삭제하는 '만료 기능'의 설계를 구상해 보세요.

}