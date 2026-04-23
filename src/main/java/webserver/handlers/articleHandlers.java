package webserver.handlers;

import annotations.RequestMapping;
import db.DBEntryPoint;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.Article;
import model.User;
import webserver.session.Session;

import java.io.IOException;
import java.time.LocalDateTime;

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
        User sessionUser = (User)session.getAttribute("user");

        String title = request.getBodyParam("title");
        String contentBody = request.getBodyParam("content");
        String authorName = sessionUser.getName();
        int authorIdIdx = sessionUser.getId();
        LocalDateTime currentTime = LocalDateTime.now();

        database.addArticle(title, contentBody, authorName, authorIdIdx, currentTime);

        response.sendRedirect("/");
        return null;
    }

}
