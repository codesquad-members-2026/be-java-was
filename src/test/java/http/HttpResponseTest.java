package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpResponseTest {

    @Test
    @DisplayName("존재하는 정적 파일(/index.html)을 요청하면 200 OK와 파일 내용이 담겨야 한다.")
    void response200OkForExistingFile() {
        // given: 실제로 static 폴더 안에 존재하는 파일 경로
        String path = "/index.html";

        // when: HttpResponse 객체 생성 (이때 내부적으로 파일을 읽음)
        HttpResponse response = HttpResponse.of(path);

        // then: 상태 코드가 200 OK 이고, 바디에 데이터가 존재해야 함
        assertThat(response.getStatusCode()).isEqualTo("200 OK");
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().length).isGreaterThan(0);

        // Mime 클래스 구현에 따라 다르겠지만, Content-Type이 null이 아님을 확인
        assertThat(response.getContentType()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 파일을 요청하면 404 Not Found 상태를 반환해야 한다.")
    void response404ForNonExistingFile() {
        // given: 절대 존재하지 않을 것 같은 이상한 파일 경로
        String fakePath = "/ghost-file-not-exist.html";

        // when
        HttpResponse response = HttpResponse.of(fakePath);

        // then: 예외가 터지는 대신 404 상태코드와 404 에러 페이지 내용이 바디에 담겨야 함
        assertThat(response.getStatusCode()).isEqualTo("404 Not Found");
        assertThat(response.getBody()).isNotEmpty();

        // (참고) 404.html 파일이 있다면 그 내용이, 없다면 fallbackMessage("404 Not Found")가 들어있을 것입니다.
    }

    @Test
    @DisplayName("getCoreResponse 호출 시 헤더 정보(상태코드, 컨텐츠타입, 길이)가 규격에 맞게 문자열로 조합되어야 한다.")
    void formatCoreResponseString() {
        // given
        String path = "/index.html";
        HttpResponse response = HttpResponse.of(path);

        // when
        String coreResponse = response.getCoreResponse();

        // then: HTTP 헤더 규격에 맞게 조립되었는지 확인
        assertThat(coreResponse).contains("Status-Code: 200 OK");
        assertThat(coreResponse).contains("Content-Type: " + response.getContentType());
        assertThat(coreResponse).contains("Content-Length: " + response.getBody().length);
    }
}