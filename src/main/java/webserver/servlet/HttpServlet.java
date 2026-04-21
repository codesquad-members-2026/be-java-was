package webserver.servlet;

import java.io.IOException;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

public interface HttpServlet {
    void service(HttpRequest request, HttpResponse response) throws IOException;
}
