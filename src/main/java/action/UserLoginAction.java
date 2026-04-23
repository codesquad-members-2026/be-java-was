package action;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.SessionManager;

import java.io.IOException;

public class UserLoginAction implements Action{

    private static final Logger logger = LoggerFactory.getLogger(UserLoginAction.class);

    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        User user = Database.findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            String sid = SessionManager.getInstance().createSession(user);

            logger.debug("로그인 성공: 사용자 ID=[{}], 이름=[{}], 세션 ID=[{}]", user.getUserId(), user.getName(), sid);

            response.addHeader("Set-Cookie", "SID=" + sid + "; Path=/");
            response.sendRedirect("/index.html");
            return;
        }
        logger.debug("로그인 실패: ID=[{}]", userId);
        response.sendRedirect("/user/login_failed.html");
    }
}