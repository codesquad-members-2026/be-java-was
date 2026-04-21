package webserver.handlers;

import annotations.RequestMapping;
import db.DBEntryPoint;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import webserver.session.Session;

import java.io.IOException;

public class articleHandlers {
    @RequestMapping(method="GET", path="/article")
    public String getArticleSubmissionPage(HttpResponse response, Session session) throws IOException {
        if(session == null){
            response.sendRedirect("/login");
            return null;
        }
        response.sendHtml("/article/index.html");
        return null;
    }

    @RequestMapping(method = "POST", path = "/article")
    public String postArticleSubmittedByUser(HttpRequest request, HttpResponse response, Session session, DBEntryPoint database){
        if(session == null){
            response.sendRedirect("/login");
            return null;
        }

        // TODO : post it into DB and redirect to main page
        return null;
    }

}
