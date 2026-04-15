package webserver.session;

import java.util.HashMap;
import java.util.Map;

public class Session {
    private String id;
    private Map<String, Object> attributes = new HashMap<>();

    public Session(String id){
        this.id = id;
    }

    public void addAttribute(String key, Object val) {
        this.attributes.put(key,val);
    }

    public Object getAttribute(String key){
        return this.attributes.get(key);
    }

    public String getId(){
        return this.id;
    }


}
