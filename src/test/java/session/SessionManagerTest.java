package session;

import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sessionManager;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 전마다 싱글톤 인스턴스를 가져오고 테스트 유저를 준비합니다.
        sessionManager = SessionManager.getInstance();
        testUser = new User("wonjae", "1234", "원재", "wonjae@test.com");
    }

    @Test
    @DisplayName("로그인 후 로그아웃하면 세션 저장소에서 유저 정보가 완전히 삭제되어야 한다")
    void logout_ShouldRemoveUserFromSession() {
        // [1] 로그인 시뮬레이션: 세션을 생성하고 ID를 받습니다.
        String sid = sessionManager.createSession(testUser);

        // [2] 중간 확인: 지금은 유저가 검색되어야 합니다.
        assertNotNull(sessionManager.read(sid), "세션 생성 직후에는 유저가 존재해야 합니다.");

        // [3] 로그아웃 시뮬레이션: 세션을 삭제합니다.
        sessionManager.delete(sid);

        // [4] 최종 검증: 삭제된 ID로 조회했을 때 null이 나와야 로그아웃 성공!
        User deletedUser = sessionManager.read(sid);
        assertNull(deletedUser, "로그아웃 후에는 해당 SID로 유저를 찾을 수 없어야 합니다.");
    }
}