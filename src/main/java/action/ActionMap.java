package action;

import java.util.HashMap;
import java.util.Map;

public class ActionMap {

    private static final Map<String, Action> map = new HashMap<>();

    static {

        map.put("/user/create", new UserCreateAction());
        map.put("/user/login", new UserLoginAction());
    }

    public static Action getAction(String path) {
        return map.get(path);
    }
}
