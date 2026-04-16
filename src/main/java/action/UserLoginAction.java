package action;

import db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.request.HttpRequest;
import webserver.response.ResponseData;

import java.util.UUID;

public class UserLoginAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginAction.class);

    @Override
    public ResponseData process(HttpRequest request) {
        String userId = request.getBodies().get("userId");
        String password = request.getBodies().get("password");

        // TODO: 로그인이 실패하면 /user/login_failed.html로 이동한다.
        if(!Database.isExistUserById(userId)){
            logger.error("{} is an unregistered member", userId);
            return ResponseData.of("redirect:/user/register.html");
        }

        if(!Database.checkPasswordMatching(userId, password)){
            logger.error("{} password is not match", userId);
            return ResponseData.of("redirect:/user/login.html");
        }

        logger.debug("User login successfully");
        String sessionId = "JSESSIONID=" + UUID.randomUUID() + "; Path=/";
        return ResponseData.of("redirect:/main/index.html").addHeader("Set-Cookie", sessionId);
    }
}
