package webserver;

import java.io.*;
import java.net.Socket;

import action.Action;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static utils.HttpConstant.CRLF;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

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
            String method = httpRequest.getMethod();
            String rawPath = httpRequest.getPath();
            logger.debug(httpRequest.getCoreRequestInfo());

            Action action = Router.getAction(method, rawPath);
            String routedPath = (action != null) ? action.process(httpRequest) : Router.convertStaticPath(rawPath);

            HttpResponse httpResponse = HttpResponse.of(routedPath);
            byte[] body = httpResponse.getBody();
            logger.debug("[Server's Response Message]");
            logger.debug(httpResponse.getCoreResponse());

            responseHeader(dos, body.length,
                    httpResponse.getContentType(), httpResponse.getStatusCode(), httpRequest.getProtocol());
            responseBody(dos, body);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
    // TODO: 분리 필요
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
            dos.flush(); // TODO: flush를 남겨야 하는 이유 자세히 알아보기
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
