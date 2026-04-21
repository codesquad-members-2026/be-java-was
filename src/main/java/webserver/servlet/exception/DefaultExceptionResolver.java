package webserver.servlet.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.exception.PageNotFoundException;
import webserver.servlet.ResourceRenderer;

public class DefaultExceptionResolver implements ExceptionResolver {
    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionResolver.class);

    private final ResourceRenderer renderer;

    public DefaultExceptionResolver(ResourceRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void resolve(HttpRequest request, HttpResponse response, Exception ex) {
        try {
            if (ex instanceof PageNotFoundException) {
                response.setStatusCode(404);
                renderer.render("/error/404.html", response);
                return;
            }
            response.setStatusCode(500);
            renderer.render("/error/500.html", response);
        } catch (Exception e) {
            logger.error("에러 페이지 렌더링 실패", e);
            throw new RuntimeException(e);
        }
    }
}
