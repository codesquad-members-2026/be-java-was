package action;

import java.util.HashMap;
import java.util.Map;

public class ActionMap {

    private static final Map<String, Action> map = new HashMap<>();

    static {
        // [분석] 서버 시작 시점에 모든 액션 객체가 미리 생성되어 메모리에 로드됩니다.
        map.put("/user/create", new UserCreateAction());
        map.put("/user/login", new UserLoginAction());
        map.put("/user/logout", new UserLogoutAction());
    }

    public static Action getAction(String path) {
        // TODO: 만약 사용자가 끝에 슬래시를 붙여 "/user/login/"으로 요청한다면 어떻게 될까요?
        // 경로의 일관성을 유지하기 위해 주소를 정규화하거나 예외 처리를 하는 로직을 고민해 보세요.
        return map.get(path);
    }

}