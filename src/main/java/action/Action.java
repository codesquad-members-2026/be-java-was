package action;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public interface Action {
    // TODO: 현재는 void 형식이지만, 실행 결과(성공/실패 여부 등)를 반환하도록 설계하면
    // 공통적인 후속 처리(로깅, 통계 등)를 하기에 더 유리해질 수 있습니다.
    void execute(HttpRequest request, HttpResponse response) throws IOException;
}