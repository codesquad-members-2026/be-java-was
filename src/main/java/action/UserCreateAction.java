package action;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UserCreateAction implements Action {
    private static final Logger logger = LoggerFactory.getLogger(UserCreateAction.class);

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        if (!"POST".equals(request.getMethod())) {
            logger.error("GET 방식으로는 회원가입이 되지 않습니다.");
            response.sendRedirect("/registration/index.html");
            return;
        }

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        // TODO: 필수 파라미터(userId, password 등)가 비어있는지(null or empty) 검증하는 로직을 추가해 보세요.

        // TODO: 이미 가입된 아이디인지 Database에서 확인하는 중복 체크 로직을 추가해 보세요.

        User user = new User(userId, password, name, email);

        // TODO: 보안을 위해 비밀번호를 평문으로 저장하지 않고 암호화(Hashing)하여 저장하는 방법을 고민해 보세요.
        Database.addUser(user);

        logger.debug("회원가입 성공: {}", user);

        response.sendRedirect("/index.html");
    }
}