package webserver;

import java.util.HashMap;
import java.util.Map;

public class ResourceHandler {
    private static final Map<String, String> resource = new HashMap<>();

    static {
        resource.put("/", "/index.html");
        resource.put("/registration", "/registration/index.html");
        resource.put("/register.html", "/registration/index.html");
    }

    public static String getResourcePath(String url) {
        return resource.getOrDefault(url, url);
    }
}
