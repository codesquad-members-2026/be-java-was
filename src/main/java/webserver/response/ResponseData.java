package webserver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

public class ResponseData {
    private final byte[] body;
    private final Map<String, String> headers;
    private StatusLine statusLine;

    private static final Map<Class<? extends Exception>, ErrorConfig> errors = getErrors();

    private static final String HTML_EXTENSION = "html";
    private static final String REDIRECT = "redirect:";
    private static final String STATIC_PATH = "src/main/resources/static";

    private static final Logger logger = LoggerFactory.getLogger(ResponseData.class);

    private ResponseData(byte[] body, Map<String, String> headers, StatusLine statusLine) {
        this.body = body;
        this.headers = headers;
        this.statusLine = statusLine;
    }

    public static ResponseData of(String path){
        Map<String,String> headers = new HashMap<>();
        byte[] body = new byte[0];
        String contentLength = body.length + "";
        String absolutePath = STATIC_PATH + path;

        // addHeader로 통합 가능?
        if(path.startsWith(REDIRECT)){
            path = path.substring(REDIRECT.length()); // redirect 제거
            headers.put("Location", path);
            headers.put("Content-Length", contentLength);
            return new ResponseData(body, headers, new StatusLine(StatusCode.FOUND.getStatusCode(), ""));
        }

        String extension = extractExtension(path);
        headers.put("Content-Type", getMime(extension));
        logger.debug("absolutePath: {}", absolutePath);

        try {
            body = getFileByteData(absolutePath);
            contentLength = body.length + "";
            headers.put("Content-Length", contentLength);
            return new ResponseData(body, headers, new StatusLine(StatusCode.OK.getStatusCode(), ""));

        } catch (IOException ie){
            ErrorConfig errorConfig = errors.getOrDefault(ie.getClass(), errors.get(IOException.class));
            body = getSafeErrorPage(errorConfig.path(), errorConfig.statusCode());
            contentLength = body.length + "";
            headers.put("Content-Length", contentLength);
            headers.put("Content-Type", getMime(HTML_EXTENSION));
            return new ResponseData(body, headers, new StatusLine(errorConfig.statusCode(), ""));
        }
    }

    public ResponseData addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }
    public ResponseData addProtocol(String protocol) {
        this.statusLine = new StatusLine(this.statusLine.statusCode(), protocol);
        return this;
    }

    private static Map<Class<? extends Exception>, ErrorConfig> getErrors() {
        Map<Class<? extends Exception>, ErrorConfig> errors = new HashMap<>();

        StatusCode status404 = StatusCode.NOT_FOUND;
        StatusCode status403 = StatusCode.FORBIDDEN;
        StatusCode status500 = StatusCode.SERVER_ERROR;

        errors.put(NoSuchFileException.class, new ErrorConfig(status404.getPath(), status404.getStatusCode()));
        errors.put(AccessDeniedException.class, new ErrorConfig(status403.getPath(), status403.getStatusCode()));
        errors.put(IOException.class, new ErrorConfig(status500.getPath(), status500.getStatusCode()));

        return errors;
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

    public InputStream getBodyStream() {
        return new ByteArrayInputStream(this.body);
    }
    public Map<String, String> getHeaders() {
        return Map.copyOf(this.headers);
    }
    public StatusLine getStatusLine() {
        return this.statusLine;
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
