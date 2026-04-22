package webserver.servlet;

import java.io.IOException;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.exception.PageNotFoundException;
import webserver.response.ResponseRenderer;
import webserver.response.ResponseResolver;
import webserver.response.TemplateData;
import webserver.servlet.handler.HandlerMappings;
import webserver.servlet.handler.HandlerMethod;
import webserver.session.SessionManager;

public class DispatcherServlet implements HttpServlet {
    private final HandlerMappings handlerMappings;
    private final SessionManager sessionManager;
    private final ResponseResolver responseResolver;

    public DispatcherServlet(ResponseResolver responseResolver, HandlerMappings handlerMappings,
                             SessionManager sessionManager) {
        this.handlerMappings = handlerMappings;
        this.responseResolver = responseResolver;
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

        TemplateData data = new TemplateData();
        String resource = handlerMethod.execute(request, response, data, sessionManager);
        ResponseRenderer renderer = responseResolver.resolve(resource);
        renderer.render(resource, response, data);
    }
}
