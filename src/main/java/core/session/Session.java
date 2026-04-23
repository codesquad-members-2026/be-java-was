package core.session;

import java.util.HashMap;
import java.util.Map;

public class Session {
    private String id;
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private long creationTime;
    private long lastAccessedTime;
    private int maxInactiveInterval;

    public Session(String id){
        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = this.creationTime;
        this.maxInactiveInterval = 1800; // 30분
    }

    public void access(){
        this.lastAccessedTime = System.currentTimeMillis();
    }
    public boolean isExpired(){
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastAccessedTime) >= (maxInactiveInterval * 1000L);
    }
    public void setAttributes(String name, Object value){
        this.attributes.put(name, value);
    }

    public Object getAttribute(String name){
        return this.attributes.get(name);
    }
}
