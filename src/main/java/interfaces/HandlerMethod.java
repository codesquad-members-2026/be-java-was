package interfaces;

import http.HttpRequest;
import http.HttpResponse;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface HandlerMethod {
    public void handle(HttpRequest request, HttpResponse response) throws InvocationTargetException, IllegalAccessException;
}
