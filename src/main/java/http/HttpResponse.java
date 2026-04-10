package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

public class HttpResponse {
    private final byte[] body;
    private final String statusCode;
    private final String contentType;

    private static final String INDEX_EXTENSION = "html";
    private static final String STATIC_PATH = "src/main/resources/static/";
    private static final String DOT = ".";
    private static final String CRLF = "\r\n";

    // TODO: 추후 정리 대상
    private static final String PATH_404 = "src/main/resources/static/status/404.html";
    private static final String PATH_403 = "src/main/resources/static/status/403.html";
    private static final String PATH_500 = "src/main/resources/static/status/500.html";
    private static final String MSG_200 = "200 OK";
    private static final String MSG_403 = "403 Forbidden";
    private static final String MSG_404 = "404 Not Found";
    private static final String MSG_500 = "500 Internal Server Error";

    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private HttpResponse(byte[] body, String extension, String statusCode) {
        this.body = body;
        this.statusCode = statusCode;
        this.contentType = getMime(extension);
    }

    private static HttpResponse ok(byte[] body, String extension) {
        return new HttpResponse(body, extension, MSG_200);
    }

    // Error
    private static HttpResponse notFound(byte[] body) {
        return new HttpResponse(body, INDEX_EXTENSION, MSG_404);
    }
    private static HttpResponse forbidden(byte[] body) {
        return new HttpResponse(body, INDEX_EXTENSION, MSG_403);
    }
    private static HttpResponse internalServerError(byte[] body) {
        return new HttpResponse(body, INDEX_EXTENSION, MSG_500);
    }

    // Response 메세지를 만들어서 반환
    public static HttpResponse of(String path) {
        String[] splits = splitPath(path);
        String pathWithoutExtension = splits[0];
        String extension = splits[1];
        String absolutePath = STATIC_PATH + pathWithoutExtension + DOT + extension;
        logger.debug("[Absolute Path]: {}", absolutePath);

        // TODO: 상태를 처리하는 클래스 추후 분리
        byte[] body;
        try {
            body = getFileByteData(absolutePath);
            logger.debug("");
            return ok(body, extension);
        } catch (NoSuchFileException ne) {
            logger.error(MSG_404 + ": ", ne);
            body = getSafeErrorPage(PATH_404, MSG_404);
            return notFound(body);
        } catch (AccessDeniedException ae){
            logger.error(MSG_403 + ": ", ae);
            body = getSafeErrorPage(PATH_403, MSG_403);
            return forbidden(body);
        } catch (IOException ie){
            logger.error(MSG_500 + ": ", ie);
            body = getSafeErrorPage(PATH_500, MSG_500);
            return internalServerError(body);
        }
    }

    private static String[] splitPath(String path){
        int lastDotIndex = path.lastIndexOf(DOT);
        if(lastDotIndex == -1)
            return new String[]{path.substring(1), ""};

        String fileName = path.substring(1, lastDotIndex);
        String extension = path.substring(lastDotIndex + 1);

        return new String[]{fileName, extension};
    }
    private static byte[] getFileByteData(String path) throws IOException {
        File file = new File(path);
        return Files.readAllBytes(file.toPath());
    }
    private static byte[] getSafeErrorPage(String path, String fallBackMessage) {
        try {
            return getFileByteData(path);
        } catch (IOException e) {
            logger.error("에러 HTML 파일 자체를 읽을 수 없습니다. 경로: {}", path);
            return fallBackMessage.getBytes();
        }
    }
    private String getMime(String extension) {
        return Mime.getContentTypeThroughExtension(extension);
    }

    public byte[] getBody() {
        return body;
    }
    public String getStatusCode() {
        return statusCode;
    }
    public String getContentType() {
        return contentType;
    }
    public String getCoreResponse() {
        int bodyLength = body.length;

        return "Status-Code: " + this.statusCode + CRLF +
                "Content-Type: " + this.contentType + CRLF +
                "Content-Length: " + bodyLength;
    }
}
