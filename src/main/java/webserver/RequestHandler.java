package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import jdk.jfr.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            String[] tokens = line.split(" ");
            String path = tokens[1].trim();

            while (line != null && !line.isEmpty()) {
                logger.debug("header : {}", line);
                line = br.readLine();
            }

            DataOutputStream dos = new DataOutputStream(out);

            if (path.equals("/")) {
                path = "/index.html";
            }

            if (!path.contains(".")) {
                if (!path.endsWith("/")) path += "/";
                path += "index.html";
            }
            String contentType = getMimeType(path);
            byte[] body = Files.readAllBytes(new File("./src/main/resources/static" + path).toPath());
            response200Header(dos, body.length, contentType);
            responseBody(dos, body);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private String getMimeType(String path) {
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js"))  return "application/javascript";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".ico")) return "image/x-icon";

        return "text/html";
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");

            dos.writeBytes("X-Content-Type-Options: nosniff\r\n");

            dos.writeBytes("\r\n");
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
