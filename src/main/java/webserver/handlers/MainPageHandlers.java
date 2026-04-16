package webserver.handlers;

import annotations.RequestMapping;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.TemplateAttributes;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.session.Session;
import webserver.session.SessionManager;


import java.io.IOException;

public class MainPageHandlers {
    private static final Logger logger = LoggerFactory.getLogger(MainPageHandlers.class);

    @RequestMapping(method = "GET", path = "/")
    public String getFrontPage(HttpRequest request, HttpResponse response, Session session, TemplateAttributes templateAttributes) throws IOException {
        if(session == null){
            logger.debug("NULL IN LOGIN SESSION");
            response.sendInvalidSessionResponse(request.getSessionID(),"/");
            return null;
        }
        templateAttributes.setAttribute("userID", ((User)session.getAttribute("user")).getName());
        return "/main/templateVersion.html";
    }

    @RequestMapping(method = "POST", path = "/logout")
    public String logoutRedirectToFrontPage(HttpRequest request, HttpResponse response, SessionManager sessionManager) throws IOException {
        String sessionID = request.getSessionID();
        sessionManager.removeSession(sessionID);
        response.sendInvalidSessionResponse(request.getSessionID(),"/");
        return null;
    }

}
