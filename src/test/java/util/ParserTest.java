package util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    @Test
    @DisplayName("정상적인 쿼리 스트링을 넣었을 때 모든 파라미터를 정확히 분석해야 한다")
    void parseNormalQueryString() {
        String queryString = "userId=stojum&password=1111&name=Hana";
        Map<String, String> params = Parser.parseQueryString(queryString);

        assertThat(params).hasSize(3);
        assertThat(params.get("userId")).isEqualTo("stojum");
        assertThat(params.get("password")).isEqualTo("1111");
        assertThat(params.get("name")).isEqualTo("Hana");
    }

    @Test
    @DisplayName("URL 인코딩된 한글 데이터가 포함되어 있어도 올바르게 디코딩해야 한다")
    void parseEncodedKorean() {
        // "박원재"가 URL 인코딩된 상태
        String queryString = "name=%EB%B0%95%EC%9B%90%EC%9E%AC&age=25";
        Map<String, String> params = Parser.parseQueryString(queryString);

        assertThat(params.get("name")).isEqualTo("박원재");
        assertThat(params.get("age")).isEqualTo("25");
    }

    @Test
    @DisplayName("값이 없는 파라미터(key=)가 들어와도 에러 없이 빈 문자열로 처리해야 한다")
    void parseEmptyValue() {
        String queryString = "userId=stojum&password=";
        Map<String, String> params = Parser.parseQueryString(queryString);

        assertThat(params.get("userId")).isEqualTo("stojum");
        assertThat(params.get("password")).isEmpty(); // "" 빈 문자열 확인
    }

    @Test
    @DisplayName("쿼리 스트링이 null이거나 비어있을 경우 빈 맵을 반환해야 한다")
    void parseNullOrEmpty() {
        assertThat(Parser.parseQueryString(null)).isEmpty();
        assertThat(Parser.parseQueryString("")).isEmpty();
    }
}