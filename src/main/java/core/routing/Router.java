package core.routing;

import app.action.*;
import app.user.User;
import core.session.Session;
import core.session.SessionManager;
import core.request.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class Router {
    private static final Map<String, String> staticUrlMaps = new HashMap<>();
    private static final Map<String, Action> actions = new HashMap<>();

    private static final String HTML_EXTENSION = ".html";
    private static final String BLANK_SEPARATOR = " ";

    static {
        staticUrlMaps.put("/", "/index.html");
        staticUrlMaps.put("/static", "/index.html");

        staticUrlMaps.put("/registration", "/user/register.html");
        staticUrlMaps.put("/register.html", "/user/register.html");

        staticUrlMaps.put("/user", "/user/login.html");
        staticUrlMaps.put("/login.html", "/user/login.html");

        actions.put("POST /create", new UserCreateAction());
        actions.put("POST /login", new UserLoginAction());
        actions.put("POST /logout", new UserLogoutAction());
        actions.put("GET /user/list", new UserListAction());
    }

    public static RoutedInfo route(HttpRequest httpRequest) {
        Action action = getAction(httpRequest);
        String originPath = httpRequest.getStartLine().path();
        RoutedInfo routedInfo;

        if(action != null){
            routedInfo = action.process(httpRequest);
        } else {
            String mappedPath = staticUrlMaps.getOrDefault(originPath, originPath);
            RouteType type = mappedPath.endsWith(HTML_EXTENSION) ? RouteType.DYNAMIC : RouteType.STATIC;
            routedInfo = RoutedInfo.of(mappedPath, type, new HashMap<>(), new HashMap<>());
        }

        return (routedInfo.routeType() == RouteType.DYNAMIC) ? appendGlobalModels(httpRequest, routedInfo) : routedInfo;
    }

    private static Action getAction(HttpRequest httpRequest) {
        String method = httpRequest.getStartLine().method();
        String path = httpRequest.getStartLine().path();
        return actions.get(method + BLANK_SEPARATOR + path);
    }

    private static RoutedInfo appendGlobalModels(HttpRequest httpRequest, RoutedInfo beforeRoutedInfo) {
        Session session = extractSession(httpRequest);
        Map<String, Object> completedModel = new HashMap<>(beforeRoutedInfo.models());
        User user = session != null ? (User) session.getAttribute("user") : null;

        if(user != null){
            completedModel.put("userName", user.getName());
            completedModel.put("showLogin", "none");
            completedModel.put("showLogout", "flex");
        } else {
            completedModel.put("userName", "");
            completedModel.put("showLogin", "flex");
            completedModel.put("showLogout", "none");
        }

        return new RoutedInfo(beforeRoutedInfo.routedPath(), beforeRoutedInfo.routeType(),
                beforeRoutedInfo.headers(), beforeRoutedInfo.queries(), completedModel);
    }

    private static Session extractSession(HttpRequest httpRequest) {
        String sessionID = httpRequest.getHeaders().getSessionId();

        if(!sessionID.isEmpty() && SessionManager.isValid(sessionID)){
            return SessionManager.getSession(sessionID);
        }

        return null;
    }
}