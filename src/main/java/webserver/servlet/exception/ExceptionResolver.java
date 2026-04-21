package webserver.servlet.exception;

import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

public interface ExceptionResolver {
    void resolve(HttpRequest request, HttpResponse response, Exception exception);
}
