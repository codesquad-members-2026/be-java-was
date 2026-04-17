package webserver.servlet;

import java.io.IOException;
import webserver.HttpRequest;
import webserver.HttpResponse;

public interface HttpServlet {
    void service(HttpRequest request, HttpResponse response) throws IOException;
}
