package webserver.servlet;

import java.io.IOException;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class ErrorServlet implements  HttpServlet{
    private final ResourceRenderer renderer;
    public ErrorServlet(ResourceRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        int statusCode = response.getStatusCode();
        renderer.render("/error/" + statusCode + ".html", response);
    }
}
