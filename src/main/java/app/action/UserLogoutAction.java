package app.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import core.routing.RouteType;
import core.routing.RoutedInfo;
import core.request.HttpRequest;
import core.session.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class UserLogoutAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UserLogoutAction.class);

    @Override
    public RoutedInfo process(HttpRequest request) {
        String userSessionId = request.getHeaders().getSessionId();
        String routingPath;
        Map<String, String> headers = new HashMap<>();

        if(userSessionId != null){
            SessionManager.removeSession(userSessionId);
            logger.debug("User Logout Successfully!");
        }

        routingPath = "redirect:/index.html";
        headers.put("Set-Cookie", "JSESSIONID=; Path=/; Max-Age=0");
        return RoutedInfo.of(routingPath, RouteType.REDIRECT, headers, new HashMap<>());
    }
}