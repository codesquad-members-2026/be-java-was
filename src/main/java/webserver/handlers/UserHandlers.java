package webserver.handlers;

import annotations.RequestMapping;
import db.DBEntryPoint;
import db.Database;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.TemplateAttributes;
import model.User;
import webserver.session.Session;

import java.io.IOException;


public class UserHandlers {

    @RequestMapping(method = "GET", path = "/user/list")
    public String getUserListPage(HttpRequest request, HttpResponse response, Session session, TemplateAttributes attributes, DBEntryPoint database) throws IOException {
        if(session == null){
            response.setSessionInvalidateHeader(request.getSessionID());
            response.sendRedirect("/login");
            return null;
        }
        response.setStatus("200 OK");
        attributes.setAttribute("userID", ((User)session.getAttribute("user")).getUserId());
        attributes.setAttribute("users", database.findAllUser());
        return "/user/list.html";
    }
}
