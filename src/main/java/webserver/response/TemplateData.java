package webserver.response;

import java.util.HashMap;
import java.util.Map;

public class TemplateData {
    private final Map<String, Object> map = new HashMap<>();

    public TemplateData() {
    }

    public void add(String key, Object value) {
        map.put(key, value);
    }

    public Object get(String key) {
        return map.get(key);
    }
}
