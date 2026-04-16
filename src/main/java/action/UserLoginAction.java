package action;

import db.Database;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.request.HttpRequest;
import webserver.response.ResponseData;
import webserver.session.Session;
import webserver.session.SessionManager;

import java.util.UUID;

public class UserLoginAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginAction.class);

    @Override
    public ResponseData process(HttpRequest httpRequest) {
        String oldSessionId = httpRequest.getHeaders().getSessionId();
        String userId = httpRequest.getBodies().get("userId");
        String password = httpRequest.getBodies().get("password");
        User user = Database.findUserById(userId);

        // TODO: 로그인이 실패하면 /user/login_failed.html로 이동한다.
        if(user == null){
            logger.error("{} is an unregistered member", userId);
            return ResponseData.of("redirect:/user/register.html");
        }

        if(!user.isEqualPassword(password)){
            logger.error("{} password is not match", userId);
            return ResponseData.of("redirect:/user/login.html");
        }

        logger.debug("User login successfully");
        String newSessionId = addSession(user, oldSessionId);
        return ResponseData.of("redirect:/main/index.html").addHeader("Set-Cookie", makeCookieHeader(newSessionId));
    }

    private String addSession(User user, String oldSessionId){
        String newSessionId = UUID.randomUUID() + "";
        Session session = new Session(newSessionId);
        session.setAttributes("user", user);

        if(oldSessionId != null){
            SessionManager.removeSession(oldSessionId);
        }
        SessionManager.addSession(newSessionId, session);

        return newSessionId;
    }

    private String makeCookieHeader(String newSessionId){
        return "JSESSIONID=" + newSessionId + "; Path=/";
    }
}
