package core.http;

public enum StatusCode {
    OK("", "200 OK"),
    FOUND("", "302 Found"),
    FORBIDDEN("src/main/resources/static/status/403.html", "403 Forbidden"),
    NOT_FOUND("src/main/resources/static/status/404.html", "404 Not Found"),
    NOT_ALLOWED("src/main/resources/static/status/405.html", "405 Method Not Allowed"),
    SERVER_ERROR("src/main/resources/static/status/500.html", "500 Internal Server Error");

    private final String path;
    private final String code;

    StatusCode(String path, String code) {
        this.path = path;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getPath() {
        return path;
    }
}
