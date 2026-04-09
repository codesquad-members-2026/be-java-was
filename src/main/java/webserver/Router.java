package webserver;

import java.util.HashMap;
import java.util.Map;

public class Router {
    private static final Map<String, String> urlMap = new HashMap<>();

    static {
        urlMap.put("/", "/index.html");

        urlMap.put("/registration", "/registration/register.html");
        urlMap.put("/register.html", "/registration/register.html");
    }

    public static String convertPath(String origin){
        return urlMap.getOrDefault(origin, origin);
    }
}
