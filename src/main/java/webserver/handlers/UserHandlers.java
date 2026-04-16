package webserver.handlers;

import annotations.RequestMapping;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import webserver.session.Session;

public class UserHandlers {

    @RequestMapping(method = "GET", path = "/user/list")
    public String getUserListPage(HttpRequest request, HttpResponse response, Session session){
        if(session == null){
            response.setHeader();
        }
    }
}
