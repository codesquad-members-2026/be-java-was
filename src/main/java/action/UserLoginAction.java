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

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        if (!"POST".equals(request.getMethod())) {
            logger.error("GET 방식으로는 로그인이 되지 않습니다.");
            response.sendRedirect("/login/index.html");
            return;
        }

        String userId = request.getParameter("username");
        String userPassword = request.getParameter("password");

        User user = Database.findUserById(userId);
        SessionManager sessionManager = SessionManager.getInstance();

        if (user != null && userPassword.equals(user.getPassword())) {
        logger.debug("로그인 성공: {}", userId);

        String sid = sessionManager.createSession(user);
        response.addHeader("Set-Cookie", "SID=" + sid + "; Path=/");
        response.sendRedirect("/index.html");
        return;
        }
        logger.debug("로그인 실패: {}", userId);
        response.sendRedirect("/user/login_failed.html");
    }
}
