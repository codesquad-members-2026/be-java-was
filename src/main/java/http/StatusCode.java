package http;

public enum StatusCode {
    OK("200 OK"),
    FOUND("302 Found"),
    FORBIDDEN("403 Forbidden"),
    NOT_FOUND("404 Not Found"),
    NOT_ALLOWED("405 Method Not Allowed"),
    SERVER_ERROR("500 Internal Server Error");

    private final String code;

    StatusCode(String code) {
        this.code = code;
    }

    public String getStatusCode() {
        return code;
    }
}
