package webserver.servlet;

import webserver.HttpRequest;
import webserver.HttpResponse;

public class InternalErrorServlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        // ERROR 500 페이지
    }
}
