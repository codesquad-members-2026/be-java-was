package webserver.servlet.handler;

import java.lang.reflect.Method;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.session.Session;
import webserver.session.SessionManager;

public class HandlerMethod {
    private Object handler;
    private Method method;

    public HandlerMethod(Object handler, Method method) {
        this.handler = handler;
        this.method = method;
    }

    // todo: 반환 타입 Object로 변경 후 분기 처리 & 동적 파라미터 개선
    public String execute(HttpRequest request, HttpResponse response, SessionManager sessionManager) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpRequest.class) {
                args[i] = request;
            } else if (parameterType == HttpResponse.class) {
                args[i] = response;
            } else if (parameterType == Session.class) {
                args[i] = sessionManager.get(request);
            } else {
                throw new IllegalArgumentException("지원하지 않는 파라미터: " + parameterType.getName());
            }
        }

        try {
            return (String) method.invoke(handler, args);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
