package action;

import java.util.HashMap;
import java.util.Map;

public class ActionMap {

    private static final Map<String, Action> actions = new HashMap<>();

    static {
        // [분석] 서버 시작 시점에 모든 액션 객체가 미리 생성되어 메모리에 로드됩니다.
        actions.put("/user/create", new UserCreateAction());
        actions.put("/user/login", new UserLoginAction());
        actions.put("/user/logout", new UserLogoutAction());
        actions.put("/index.html", new IndexAction());
        actions.put("/user/list", new UserListAction());
        actions.put("/", new IndexAction());

    }

    public static Action getAction(String path) {
        Action action = actions.get(path);
        if (action == null) {
            return new StaticResourceAction();
        }
        return action;
    }

}