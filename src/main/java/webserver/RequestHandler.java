package webserver;

import java.io.*;
import java.net.Socket;
import action.Action;
import action.ActionMap;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.PathUtil;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            // TODO: 만약 클라이언트가 보낸 데이터가 HTTP 형식이 아닐 경우(Bad Request)에 대한 예외 처리가 필요합니다.
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            String path = request.getPath();

            // TODO: 아래의 쿼리 스트링(?) 제거 로직은 HttpRequest 내부로 옮기는 것이 좋습니다.
            // RequestHandler는 '이미 정제된 주소'를 꺼내 쓰기만 하도록 역할을 분리(캡슐화)해 보세요.
            int index = path.indexOf("?");
            if(index != -1) {
                path = path.substring(0, index);
            }

            // [분석] 여기서 ActionMap은 새로 생성하는 것이 아니라 이미 메모리에 로드된 객체를 '조회'합니다.
            Action action = ActionMap.getAction(path);

            if (action != null) {
                action.execute(request, response);
                return;
            }

            // TODO: 정적 파일 접근 시, 허용되지 않은 경로(예: WEB-INF, 시스템 파일 등)에 대한 보안 검증 로직을 추가하면 더 안전합니다.
            path = PathUtil.normalize(path);
            response.serveFile(path);

        } catch (IOException e) {
            // TODO: 단순 에러 로그 출력 외에 브라우저에게 500(Internal Server Error) 응답을 보내주는 친절함이 필요할 수 있습니다.
            logger.error("요청 처리 중 에러 발생: {}", e.getMessage());
        }
    }
}