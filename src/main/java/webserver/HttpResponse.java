package webserver;


import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    public static void response(HttpRequest request, DataOutputStream dos) throws IOException {

        String method = request.getMethod();
        String path = request.getPath();

        File file = new File("src/main/resources/static" + path);

        byte[] body = path.getBytes();

        if (file.exists() && file.isFile()) {
            body = Files.readAllBytes(file.toPath());
            logger.debug("success to read: {}", request.getPath());
        }

        String extension = getExtension(path);

        responseHeader(dos, ContentType.getContentType(extension), body.length);
        responseBody(dos, body);
    }


    private static void responseHeader(DataOutputStream dos, ContentType contentType,
                                       int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType.getContentType() + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static String getExtension(String path) {
        if (path == null || path.lastIndexOf(".") == -1) {
            return  "";
        }

        return path.substring(path.lastIndexOf(".") + 1);
    }

}
