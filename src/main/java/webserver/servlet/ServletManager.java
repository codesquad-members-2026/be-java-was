package webserver.servlet;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.http.HttpStatus;
import webserver.resource.ResourceLoader;
import webserver.response.ResponseResolver;
import webserver.servlet.exception.DefaultExceptionResolver;
import webserver.servlet.exception.ExceptionResolver;
import webserver.servlet.handler.HandlerMappings;
import webserver.session.SessionManager;

public class ServletManager {
    private static final Logger logger = LoggerFactory.getLogger(ServletManager.class);

    private final HttpServlet staticResourceServlet;
    private final HttpServlet dispatcherServlet;
    private final ExceptionResolver exceptionResolver;

    public ServletManager(ResourceLoader loader, List<Object> handlers) {
        staticResourceServlet = new StaticResourceServlet(loader);
        dispatcherServlet = new DispatcherServlet(new ResponseResolver(), new HandlerMappings(handlers), new SessionManager());
        exceptionResolver = new DefaultExceptionResolver(loader);
    }

    public void execute(HttpRequest request, HttpResponse response) {
        try {
            HttpServlet servlet = isStaticResource(request) ? staticResourceServlet : dispatcherServlet;
            servlet.service(request, response);
        } catch (Exception e) {
            handleError(request, response, e);
        }
    }

    private void handleError(HttpRequest request, HttpResponse response, Exception e) {
        try {
            exceptionResolver.resolve(request, response, e);
        } catch (Exception exception) {
            writeFailsafeResponse(response);
        }
    }

    private void writeFailsafeResponse(HttpResponse response) {
        try {
            response.reset();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.write("<html><body><h1>500 Internal Server Error</h1></body></html>");
        } catch (Exception e) {
            logger.error("Failsafe response failed", e);
        }
    }

    private boolean isStaticResource(HttpRequest request) {
        String method = request.getMethod();
        String path = request.getPath();
        // todo: 확장자 분리하기
        return method.equals("GET") && (path.endsWith(".html") || path.endsWith(".css")
                || path.endsWith(".ico") || path.endsWith(".svg"));
    }
}
