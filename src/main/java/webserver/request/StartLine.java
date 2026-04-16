package webserver.request;

public record StartLine(String method, String path, String protocol) {
}
