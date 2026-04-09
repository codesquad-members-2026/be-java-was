package webserver;

import java.io.*;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private static final String CRLF = "\r\n";

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}",
                connection.getInetAddress(), connection.getPort());

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {

            logger.debug("[Client's HTTP Message]");
            HttpRequest httpRequest = HttpRequest.of(br);
            logger.debug(httpRequest.getCoreRequestInfo());

            // TODO: 왜 br은 HttpRequest 에서 사용된 뒤 아래의 코드에서 사용하면 오류가 발생하는지 정리

            String method = httpRequest.getMethod();
            String routedPath = Router.convertPath(httpRequest.getPath());
            String protocol = httpRequest.getProtocol();

            // TODO: GET/POST/PUT/DELETE 등 분기 처리
            if(method.equals("GET")){
                HttpResponse httpResponse = HttpResponse.of(routedPath);
                byte[] body = httpResponse.getBody();
                String contentType = httpResponse.getContentType();
                String statusCode = httpResponse.getStatusCode();
                logger.debug("[Response Message]");
                logger.debug(httpResponse.getCoreResponse());

                responseHeader(dos, body.length, contentType, statusCode, protocol);
                responseBody(dos, body);
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseHeader(DataOutputStream dos, int lengthOfBodyContent,
                                String contentType, String statusCode, String protocol) throws IOException {
        try {
            // TODO: HTTP 규약상 응답의 첫 줄(Start Line)은 반드시 [HTTP 버전] [상태 코드] [상태 메세지] 순서로 해야함
            // 잘못 구성하면 브라우저는 HTTP 메세지를 읽지 않는다.
            dos.writeBytes(protocol + " " + statusCode + CRLF);
            dos.writeBytes("Content-Type: " + contentType + CRLF);
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + CRLF);
            dos.writeBytes(CRLF);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
