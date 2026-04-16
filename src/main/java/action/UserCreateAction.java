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

        User user = new User(userId, password, name, email);
        Database.addUser(user);

        logger.debug("회원가입 성공: {}", user);

        response.sendRedirect("/index.html");
    }
}
