package webserver.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.PageNotFoundException;
import webserver.annotation.Mapping;

public class DispatcherServlet implements HttpServlet {
    private final Map<String, HandlerMethod> pathMap;
    private final ResourceRenderer renderer;

    public DispatcherServlet(ResourceRenderer renderer, List<Object> handlers) {
        this.pathMap = new HashMap<>();
        this.renderer = renderer;
        initialize(handlers);
    }

    private void initialize(List<Object> handlers) {
        for (Object handler : handlers) {
            Method[] methods = handler.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Mapping.class)) {
                    String path = method.getAnnotation(Mapping.class).value();
                    if (pathMap.containsKey(path)) {
                        throw new IllegalStateException("중복: " + path + " 메서드: " + pathMap.get(path) + ", " + method);
                    }
                    pathMap.put(path, new HandlerMethod(handler, method));
                }
            }
        }
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();
        HandlerMethod handlerMethod = pathMap.get(path);
        if (handlerMethod == null) {
            throw new PageNotFoundException("페이지를 찾을 수 없음: ");
        }

        String resource = handlerMethod.execute(request, response);
        renderer.render(resource, response);
    }
}
