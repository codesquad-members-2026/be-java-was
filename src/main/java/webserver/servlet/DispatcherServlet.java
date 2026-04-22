package webserver.servlet;

import java.io.IOException;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.exception.PageNotFoundException;
import webserver.servlet.handler.HandlerMappings;
import webserver.servlet.handler.HandlerMethod;
import webserver.session.SessionManager;

public class DispatcherServlet implements HttpServlet {
    private final HandlerMappings handlerMappings;
    private final ResourceRenderer renderer;
    private final SessionManager sessionManager;

    public DispatcherServlet(ResourceRenderer renderer, HandlerMappings handlerMappings,
                             SessionManager sessionManager) {
        this.handlerMappings = handlerMappings;
        this.renderer = renderer;
        this.sessionManager = sessionManager;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String httpMethod = request.getMethod();
        String path = request.getPath();
        HandlerMethod handlerMethod = handlerMappings.find(httpMethod, path);
        if (handlerMethod == null) {
            throw new PageNotFoundException("페이지를 찾을 수 없음: ");
        }

        String resource = handlerMethod.execute(request, response, sessionManager);
        renderer.render(resource, response);
    }
}
