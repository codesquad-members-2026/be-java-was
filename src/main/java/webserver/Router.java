package webserver;

import action.Action;
import action.UserCreateAction;

import java.util.HashMap;
import java.util.Map;

public class Router {
    private static final Map<String, String> staticUrlMaps = new HashMap<>();
    private static final Map<String, Action> actions = new HashMap<>();

    static {
        // 정적 요청 처리
        staticUrlMaps.put("/", "/index.html");
        staticUrlMaps.put("/registration", "/registration/register.html");
        staticUrlMaps.put("/register.html", "/registration/register.html");
    
        // 동적 요청 처리
        actions.put("GET /create", new UserCreateAction());
    }

    public static String convertStaticPath(String originalPath) {
        return staticUrlMaps.getOrDefault(originalPath, originalPath);
    }

    public static Action getAction(String method, String path){
        return actions.get(method + " " + path);
    }
}
