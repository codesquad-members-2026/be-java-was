package webserver.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cookie 헤더 값 포맷")
class CookieTest {

    @Test
    @DisplayName("name=value 형식의 문자열로 변환한다")
    void toHeaderValueBasic() {
        Cookie cookie = new Cookie("sessionId", "abc123", null, null);

        assertThat(cookie.toHeaderValue()).isEqualTo("sessionId=abc123");
    }

    @Test
    @DisplayName("Path 속성이 있으면 Path 속성을 포함한다")
    void toHeaderValueWithPath() {
        Cookie cookie = new Cookie("sessionId", "abc123", "/", null);

        assertThat(cookie.toHeaderValue()).isEqualTo("sessionId=abc123; Path=/");
    }

    @Test
    @DisplayName("Max-Age 속성이 있으면 Max-Age 속성을 포함한다")
    void toHeaderValueWithMaxAge() {
        Cookie cookie = new Cookie("sessionId", "abc123", null, 7200);

        assertThat(cookie.toHeaderValue()).isEqualTo("sessionId=abc123; Max-Age=7200");
    }

    @Test
    @DisplayName("Path와 Max-Age 속성을 모두 포함한다")
    void toHeaderValueWithAllAttributes() {
        Cookie cookie = new Cookie("sessionId", "abc123", "/", 7200);

        assertThat(cookie.toHeaderValue()).isEqualTo("sessionId=abc123; Path=/; Max-Age=7200");
    }
}
