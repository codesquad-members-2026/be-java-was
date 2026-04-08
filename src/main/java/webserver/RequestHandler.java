package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

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

            // TODO: 사용자 요청 처리
            String method = httpRequest.getMethod();
            String path = httpRequest.getPath();
            if(method.equals("GET")){
                // TODO: "."이 없는 내용들이 들어온다면?
                String[] requestResource = path.split("\\.");
                String fileName = requestResource[0];
                String extension = requestResource[1];

                // 방어로직 1
                if(fileName.equals("/")){
                    fileName = "/index";
                    extension = ".html";
                }

                String targetUrl = "src/main/resources/static" + fileName + "." + extension;

                File file = new File(targetUrl);

                // 방어로직 2
                if(!file.exists()){
                    return;
                }

                byte[] body = Files.readAllBytes(file.toPath());
                response200Header(dos, body.length, extension);
                responseBody(dos, body);
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String extension) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
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
