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
            SessionManager.getInstance().delete(sid);

            logger.debug("로그아웃 성공: 세션 ID=[{}] 가 삭제되었습니다.", sid);

            response.addHeader("Set-Cookie", "SID=" + sid + "; Path=/; Max-Age=0");
        } else {
            logger.debug("로그아웃 시도: 유효한 세션 쿠키가 없습니다.");
        }

        response.sendRedirect("/index.html");
    }
}