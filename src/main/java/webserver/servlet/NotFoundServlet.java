package webserver.servlet;

import webserver.HttpRequest;
import webserver.HttpResponse;

public class NotFoundServlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        // ERROR 404 페이지
    }
}
