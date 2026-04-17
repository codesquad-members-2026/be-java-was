package webserver.servlet;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webserver.annotation.GetMapping;
import webserver.annotation.PostMapping;

public class HandlerMappings {
    private final Map<String, Map<String, HandlerMethod>> methodMap = new HashMap<>();

    public HandlerMappings(List<Object> handlers) {
        methodMap.put("GET", new HashMap<>());
        methodMap.put("POST", new HashMap<>());
        initialize(handlers);
    }

    private void initialize(List<Object> handlers) {
        for (Object handler : handlers) {
            Method[] methods = handler.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    register("GET", method.getAnnotation(GetMapping.class).value(), handler, method);
                } else if (method.isAnnotationPresent(PostMapping.class)) {
                    register("POST", method.getAnnotation(PostMapping.class).value(), handler, method);
                }
            }
        }
    }

    private void register(String httpMethod, String path, Object handler, Method method) {
        Map<String, HandlerMethod> getPathMap = methodMap.get(httpMethod);
        if (getPathMap.containsKey(path)) {
            throw new IllegalStateException("중복: " + path + " 메서드: " + getPathMap.get(path) + ", " + method);
        }
        getPathMap.put(path, new HandlerMethod(handler, method));
    }

    public HandlerMethod find(String httpMethod, String path) {
        Map<String, HandlerMethod> pathMap = methodMap.get(httpMethod);
        if (pathMap == null) {
            return null;
        }
        return pathMap.get(path);
    }


}
