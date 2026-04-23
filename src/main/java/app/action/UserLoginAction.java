package app.action;

import db.Database;
import app.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import core.routing.RouteType;
import core.routing.RoutedInfo;
import core.request.HttpRequest;
import core.session.Session;
import core.session.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserLoginAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginAction.class);

    @Override
    public RoutedInfo process(HttpRequest httpRequest) {
        String routingPath;
        Map<String, String> headers = new HashMap<>();
        String oldSessionId = httpRequest.getHeaders().getSessionId();
        String userId = httpRequest.getBodies().get("userId");
        String password = httpRequest.getBodies().get("password");
        User user = Database.findUserById(userId);

        if(user == null){
            logger.error("{} is an unregistered member", userId);
            routingPath = "redirect:/user/register.html";
            return RoutedInfo.of(routingPath, RouteType.REDIRECT, headers, new HashMap<>());
        }

        if(!user.isEqualPassword(password)){
            logger.error("{} password is not match", userId);
            routingPath = "redirect:/user/login.html";
            return RoutedInfo.of(routingPath, RouteType.REDIRECT, headers, new HashMap<>());
        }

        logger.debug("User login successfully");
        String newSessionId = addSession(user, oldSessionId);
        routingPath = "redirect:/index.html";
        headers.put("Set-Cookie", makeCookieHeader(newSessionId));
        return RoutedInfo.of(routingPath, RouteType.REDIRECT, headers, new HashMap<>());
    }

    private String addSession(User user, String oldSessionId){
        // TODO: Spring의 Session이 정확히 무엇을 의미하는지 알기
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
