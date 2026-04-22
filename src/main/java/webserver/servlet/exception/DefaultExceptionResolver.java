package webserver.servlet.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.exception.PageNotFoundException;
import webserver.http.HttpStatus;
import webserver.resource.ResourceLoader;

public class DefaultExceptionResolver implements ExceptionResolver {
    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionResolver.class);

    private final ResourceLoader resourceLoader;

    public DefaultExceptionResolver(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void resolve(HttpRequest request, HttpResponse response, Exception ex) {
        response.reset();
        try {
            if (ex instanceof PageNotFoundException) {
                response.setStatusCode(HttpStatus.NOT_FOUND);
                response.write(resourceLoader.loadAsBytes("/error/404.html"));
                return;
            }
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.write(resourceLoader.loadAsBytes("/error/500.html"));
        } catch (Exception e) {
            logger.error("에러 페이지 렌더링 실패", e);
            throw new RuntimeException(e);
        }
    }
}
