package webserver.servlet;

import java.io.IOException;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class StaticResourceServlet implements HttpServlet {
    private final ResourceRenderer renderer;

    public StaticResourceServlet(ResourceRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        renderer.render(request.getPath(), response);
    }
}
