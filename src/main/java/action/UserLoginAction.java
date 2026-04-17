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

        // TODO: HTML Form의 input name 속성이 "username"과 "password"가 맞는지 다시 한번 확인해 보세요.
        String userId = request.getParameter("username");
        String userPassword = request.getParameter("password");

        User user = Database.findUserById(userId);
        SessionManager sessionManager = SessionManager.getInstance();

        if (user != null && userPassword.equals(user.getPassword())) {
            // TODO: 회원가입 시 비밀번호를 암호화했다면, 여기서도 입력받은 비밀번호를 암호화하여 비교해야 합니다.
            logger.debug("로그인 성공: {}", userId);

            String sid = sessionManager.createSession(user);

            // TODO: 보안을 위해 브라우저의 스크립트가 쿠키에 접근하지 못하도록 'HttpOnly' 옵션을 추가하는 것을 고민해 보세요.
            response.addHeader("Set-Cookie", "SID=" + sid + "; Path=/");
            response.sendRedirect("/index.html");
            return;
        }

        // TODO: 로그인 실패 시, 사용자에게 왜 실패했는지(아이디 부재 혹은 비밀번호 불일치)를
        // 쿼리 스트링이나 세션을 통해 전달할 방법이 있을지 고민해 보세요.
        logger.debug("로그인 실패: {}", userId);
        response.sendRedirect("/user/login_failed.html");
    }
}