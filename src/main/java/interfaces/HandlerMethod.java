package interfaces;

import http.MyHttpRequest;
import http.MyHttpResponse;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface HandlerMethod {
    public void handle(MyHttpRequest request, MyHttpResponse response) throws InvocationTargetException, IllegalAccessException;
}
