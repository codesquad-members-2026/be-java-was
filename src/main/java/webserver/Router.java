package webserver;

import action.Action;
import action.UserCreateAction;
import action.UserLoginAction;
import action.UserLogoutAction;
import webserver.request.HttpRequest;
import webserver.response.ResponseData;
import webserver.session.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class Router {
    private static final Map<String, ResponseData> staticUrlMaps = new HashMap<>();
    private static final Map<String, Action> actions = new HashMap<>();

    // TODO: static 블럭에 ResponseData의 of()를 실행하면 파일을 읽는 매우 큰 작업들이 한 번에 진행됌(OverHead up!) -> 추후 리팩토링 필요
    static {
        // 정적 요청 처리
        staticUrlMaps.put("/", ResponseData.of("/index.html"));

        staticUrlMaps.put("/registration", ResponseData.of("/user/register.html"));
        staticUrlMaps.put("/register.html", ResponseData.of("/user/register.html"));

        staticUrlMaps.put("/user", ResponseData.of("/user/login.html"));
        staticUrlMaps.put("/login.html", ResponseData.of("/user/login.html"));
    
        // 동적 요청 처리
        actions.put("POST /create", new UserCreateAction());
        actions.put("POST /login", new UserLoginAction());
        actions.put("POST /logout", new UserLogoutAction());
    }

    public static ResponseData makeResponseData(HttpRequest httpRequest) {
        Action action = getAction(httpRequest);
        ResponseData result = (action != null) ?
                action.process(httpRequest) : convertStaticPath(httpRequest);
        return result.addProtocol(httpRequest.getStartLine().protocol());
    }

    private static ResponseData convertStaticPath(HttpRequest httpRequest) {
        String originalPath = httpRequest.getStartLine().path();

        // 쿠키 체크 // index.html로 온 경우
        String sessionId = httpRequest.getHeaders().getSessionId();
        if(SessionManager.isValid(sessionId) && (originalPath.equals("/") || originalPath.equals("/index.html"))){
            originalPath = "/main/index.html";
        }

        return staticUrlMaps.getOrDefault(originalPath, ResponseData.of(originalPath));
    }

    private static Action getAction(HttpRequest httpRequest) {
        String method = httpRequest.getStartLine().method();
        String path = httpRequest.getStartLine().path();
        return actions.get(method + " " + path);
    }
}
