package webserver.handlers;

import annotations.RequestMapping;
import db.DBEntryPoint;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.Article;
import model.TemplateAttributes;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.session.Session;
import webserver.session.SessionManager;


import java.io.IOException;
import java.util.Collection;

public class MainPageHandlers {
    private static final Logger logger = LoggerFactory.getLogger(MainPageHandlers.class);

    @RequestMapping(method = "GET", path = "/")
    public String getFrontPage(Session session, TemplateAttributes templateAttributes, DBEntryPoint database) {
        if(session != null){
            templateAttributes.setAttribute("userID", ((User)session.getAttribute("user")).getName());
        }

        Collection<Article> articles = database.findAllArticles();

        templateAttributes.setAttribute("articles", articles);

        return "/main/templateVersion.html";
    }

    @RequestMapping(method = "POST", path = "/logout")
    public String logoutRedirectToFrontPage(HttpRequest request, HttpResponse response, SessionManager sessionManager) throws IOException {
        String sessionID = request.getSessionID();
        sessionManager.removeSession(sessionID);
        response.setSessionInvalidateHeader(sessionID);

        response.sendRedirect("/");
        return null;
    }

}
