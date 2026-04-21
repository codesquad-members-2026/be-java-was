package action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.request.HttpRequest;
import webserver.response.ResponseData;
import session.SessionManager;

public class UserLogoutAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UserLogoutAction.class);

    @Override
    public ResponseData process(HttpRequest request) {
        String userSessionId = request.getHeaders().getSessionId();

        if(userSessionId != null){
            SessionManager.removeSession(userSessionId);
            logger.debug("User Logout Successfully!");
        }

        return ResponseData.of("redirect:/index.html")
                .addHeader("Set-Cookie", "JSESSIONID=; Path=/; Max-Age=0");
    }
}