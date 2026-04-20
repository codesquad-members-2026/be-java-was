package webserver;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.servlet.DispatcherServlet;
import webserver.servlet.ErrorServlet;
import webserver.servlet.HandlerMappings;
import webserver.servlet.HttpServlet;
import webserver.servlet.ResourceRenderer;
import webserver.servlet.StaticResourceServlet;
import webserver.session.SessionManager;

public class ServletManager {
    private static final Logger logger = LoggerFactory.getLogger(ServletManager.class);

    private final HttpServlet staticResourceServlet;
    private final HttpServlet dispatcherServlet;
    private final HttpServlet errorServlet;

    public ServletManager(ResourceRenderer renderer, List<Object> handlers) {
        staticResourceServlet = new StaticResourceServlet(renderer);
        dispatcherServlet = new DispatcherServlet(renderer, new HandlerMappings(handlers), new SessionManager());
        errorServlet = new ErrorServlet(renderer);
    }

    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        try {
            HttpServlet servlet = request.isStaticResource() ? staticResourceServlet : dispatcherServlet;
            servlet.service(request, response);
        } catch (PageNotFoundException e) {
            // todo: response 조작 한 곳에서만
            response.setStatusCode(404);
            errorServlet.service(request, response);
        } catch (Exception e) {
            logger.error("status code 500: path: {} message: {}", request.getPath(), e.getMessage());
            response.setStatusCode(500);
            errorServlet.service(request, response);
        }
    }
}
