package webserver;

import action.Action;
import action.UserCreateAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RouterTest {

    @Test
    @DisplayName("등록된 정적 요청 경로가 들어오면 매핑된 목적지 경로를 반환해야 한다.")
    void convertRegisteredStaticPath() {
        // given & when & then
        assertThat(Router.convertStaticPath("/")).isEqualTo("/index.html");
        assertThat(Router.convertStaticPath("/registration")).isEqualTo("/registration/register.html");
    }

    @Test
    @DisplayName("명부에 없는 정적 경로는 원본 경로를 그대로 반환해야 한다.")
    void convertUnregisteredStaticPath() {
        // given
        String cssPath = "/css/global.css";
        String jsPath = "/js/main.js";

        // when & then
        assertThat(Router.convertStaticPath(cssPath)).isEqualTo(cssPath);
        assertThat(Router.convertStaticPath(jsPath)).isEqualTo(jsPath);
    }

    @Test
    @DisplayName("명부에 등록된 동적 요청(Method + Path)은 알맞은 Action 구현체를 반환해야 한다.")
    void getRegisteredAction() {
        // given
        String method = "GET";
        String path = "/create";

        // when
        Action action = Router.getAction(method, path);

        // then: null이 아니어야 하고, UserCreateAction의 인스턴스여야 함
        assertThat(action).isNotNull();
        assertThat(action).isInstanceOf(UserCreateAction.class);
    }

    @Test
    @DisplayName("등록되지 않은 동적 요청이 들어오면 null을 반환해야 한다.")
    void getUnregisteredAction() {
        // given & when: 등록되지 않은 HTTP 메서드나 경로
        Action postAction = Router.getAction("POST", "/create");
        Action unknownAction = Router.getAction("GET", "/login");

        // then: 모두 null을 반환해야 RequestHandler가 정적 요청으로 취급할 수 있음
        assertThat(postAction).isNull();
        assertThat(unknownAction).isNull();
    }
}