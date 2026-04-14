package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

public class ResponseData {
    private final byte[] body;
    private final Map<String, String> headers;

    private static final String HTML_EXTENSION = "html";
    private static final String REDIRECT = "redirect:";
    private static final String STATIC_PATH = "src/main/resources/static";
    private static final String PATH_404 = STATIC_PATH + "/status/404.html";
    private static final String PATH_403 = STATIC_PATH + "/status/403.html";
    private static final String PATH_500 = STATIC_PATH + "/status/500.html";

    private static final Logger logger = LoggerFactory.getLogger(ResponseData.class);

    private ResponseData(byte[] body, Map<String, String> headers) {
        this.body = body;
        this.headers = Map.copyOf(headers);
    }

    public static ResponseData of(String path){
        Map<String,String> headers = new HashMap<>();
        byte[] body = new byte[0];
        String absolutePath = STATIC_PATH + path;

        if(path.startsWith(REDIRECT)){
            path = path.substring(REDIRECT.length()); // redirect 제거
            headers.put("Location", path);
            headers.put("Status-Code", StatusCode.FOUND.getStatusCode());
            return new ResponseData(body, headers);
        }

        String extension = extractExtension(path);
        headers.put("Content-Type", getMime(extension));
        logger.debug("absolutePath: {}", absolutePath);

        // TODO: 어떻게 줄일 수 있는가
        try {
            body = getFileByteData(absolutePath);
            headers.put("Status-Code", StatusCode.OK.getStatusCode());
            return new ResponseData(body, headers);

        } catch (NoSuchFileException ne) {
            logger.error("{}: not found", ne.getMessage());
            body = getSafeErrorPage(PATH_404, StatusCode.NOT_FOUND.getStatusCode());
            headers.put("Status-Code", StatusCode.NOT_FOUND.getStatusCode());
            headers.put("Content-Type", getMime(HTML_EXTENSION));
            return new ResponseData(body, headers);

        } catch (AccessDeniedException ae){
            logger.error("{}: forbidden", ae.getMessage());
            body = getSafeErrorPage(PATH_403, StatusCode.FORBIDDEN.getStatusCode());
            headers.put("Status-Code", StatusCode.FORBIDDEN.getStatusCode());
            headers.put("Content-Type", getMime(HTML_EXTENSION));
            return new ResponseData(body, headers);

        } catch (IOException ie){
            logger.error("{}: internal server error", ie.getMessage());
            body = getSafeErrorPage(PATH_500, StatusCode.SERVER_ERROR.getStatusCode());
            headers.put("Status-Code", StatusCode.SERVER_ERROR.getStatusCode());
            headers.put("Content-Type", getMime(HTML_EXTENSION));
            return new ResponseData(body, headers);
        }
    }

    private static byte[] getFileByteData(String absolutePath) throws IOException {
        File file = new File(absolutePath);
        return Files.readAllBytes(file.toPath());
    }
    private static byte[] getSafeErrorPage(String errorPagePath, String fallBackMessage) {
        try {
            return getFileByteData(errorPagePath);
        } catch (IOException e) {
            logger.error("에러 HTML 파일 자체를 읽을 수 없습니다. 경로: {}", errorPagePath);
            return fallBackMessage.getBytes();
        }
    }
    private static String getMime(String extension) {
        return Mime.getContentTypeThroughExtension(extension);
    }
    private static String extractExtension(String path) {
        int lastDotIndex = path.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : path.substring(lastDotIndex + 1);
    }

    public byte[] getBody() {
        return body;
    }
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        this.headers.forEach((key, value)
                -> sb.append(key).append(": ").append(value).append("\n"));

        sb.append("Content-Length: ").append(body.length).append("\n");
        return sb.toString();
    }
}
