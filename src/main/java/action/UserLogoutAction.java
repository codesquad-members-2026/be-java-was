package action;

import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.SessionManager;

import java.io.IOException;

public class UserLogoutAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UserLogoutAction.class);

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        String sid = request.getCookie("SID");

        if (sid != null) {
            // [분석] 서버 메모리(SessionManager)에서 해당 세션 정보를 삭제합니다.
            SessionManager.getInstance().delete(sid);

            // TODO: 서버에서만 지우는 것이 아니라, 브라우저가 가진 쿠키도 즉시 만료(Max-Age=0)되도록
            // 응답 헤더(Set-Cookie)를 설정하는 로직을 추가해 보세요.

            logger.debug("로그아웃 성공: 세션 ID {} 삭제 완료", sid);
        }

        // TODO: 로그아웃 후 메인 화면으로 보낼지, 아니면 "로그아웃 되었습니다"라는
        // 안내 페이지로 보낼지 사용자 경험(UX) 관점에서 고민해 보세요.
        response.sendRedirect("/index.html");
    }
}