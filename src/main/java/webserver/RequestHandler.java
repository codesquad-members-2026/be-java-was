package webserver;

import java.io.*;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

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
            logger.debug(httpRequest.getAllRequest());

            // TODO: 왜 br은 HttpRequest 에서 사용된 뒤 아래의 코드에서 사용하면 오류가 발생하는지 정리

            String method = httpRequest.getMethod();
            String path = httpRequest.getPath();
            String protocol = httpRequest.getProtocol();
            if(method.equals("GET")){
                // TODO: "."이 없는 내용들이 들어온다면?
                HttpResponse httpResponse = HttpResponse.of(method, path);
                byte[] body = httpResponse.getBody();
                String extension = httpResponse.getExtension();
                String statusCode = httpResponse.getStatusCode();
                logger.debug("[Response Message Parsing Complete]");
                logger.debug("Body Length: {}", body.length);
                logger.debug("Status Code: {}", statusCode);

                responseHeader(dos, body.length, extension, statusCode, protocol);
                responseBody(dos, body);
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseHeader(DataOutputStream dos, int lengthOfBodyContent,
                                String extension, String statusCode, String protocol) throws IOException {
        try {
            // TODO: HTTP 규약상 응답의 첫 줄(Start Line)은 반드시 [HTTP 버전] [상태 코드] [상태 메세지] 순서로 해야함
            // 잘못 구성하면 브라우저는 HTTP 메세지를 읽지 않는다.
            dos.writeBytes(protocol + " " + statusCode + " \r\n");
            dos.writeBytes("Content-Type: " + getMimeType(extension) + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private String getMimeType(String extension) {
        return switch (extension) {
            case "html" -> "text/html;charset=utf-8";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "ico" -> "image/x-icon";
            case "png" -> "image/png";
            case "svg" -> "image/svg+xml";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "txt" -> "text/plain;charset=utf-8";
            default -> "application/octet-stream";
        };
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
