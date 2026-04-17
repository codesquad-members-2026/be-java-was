package model;

import java.util.HashMap;
import java.util.Map;

public class TemplateAttributes {
    Map<String, Object> attributes = new HashMap<>();

    public void setAttribute(String name, Object value){
        this.attributes.put(name,value);
    }

    public Object getAttribute(String name){
        return  this.attributes.get(name);
    }
}
