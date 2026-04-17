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
            logger.debug("로그아웃 성공: 세션 ID {} 삭제 완료", sid);
        }
        response.sendRedirect("/index.html");
    }
}