package webserver;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.servlet.DispatcherServlet;
import webserver.servlet.HttpServlet;
import webserver.servlet.InternalErrorServlet;
import webserver.servlet.NotFoundServlet;
import webserver.servlet.StaticResourceServlet;

public class ServletManager {
    private static final Logger logger = LoggerFactory.getLogger(ServletManager.class);

    private HttpServlet notFoundErrorServlet = new NotFoundServlet();
    private HttpServlet internalErrorServlet = new InternalErrorServlet();
    private final HttpServlet staticResourceServlet = new StaticResourceServlet();
    private final HttpServlet dispatcherServlet = new DispatcherServlet(null); // todo: 컨트롤러들 주입

    public ServletManager() {
    }

    public void setNotFoundErrorServlet(HttpServlet notFoundErrorServlet) {
        this.notFoundErrorServlet = notFoundErrorServlet;
    }

    public void setInternalErrorServlet(HttpServlet internalErrorServlet) {
        this.internalErrorServlet = internalErrorServlet;
    }

    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        try {
            HttpServlet servlet = request.isStaticResource() ? staticResourceServlet : dispatcherServlet;
            servlet.service(request, response);
        } catch (PageNotFoundException e) {
            notFoundErrorServlet.service(request, response);
        } catch (Exception e) {
            logger.error("예외 발생: path: {} message: {}", request.getPath(), e.getMessage());
            internalErrorServlet.service(request, response);
        }
    }
}
