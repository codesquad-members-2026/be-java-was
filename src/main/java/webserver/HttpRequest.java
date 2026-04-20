package webserver;

import java.util.Map;

public class HttpRequest {
    private final String method; // todo: enum 고려
    private final String path;
    private final String protocol;
    private final Map<String, String> queryParameters;
    private final Map<String, String> headers;
    private final byte[] body;
    private final Map<String, String> bodyMap;

    public HttpRequest(String method, String path, String protocol, Map<String, String> queryParameters,
                       Map<String, String> headers, byte[] body, Map<String, String> bodyMap) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.queryParameters = queryParameters;
        this.headers = headers;
        this.body = body;
        this.bodyMap = bodyMap;
    }

    public boolean isStaticResource() {
        // todo: 확장자 분리하기
        return method.equals("GET") && (path.endsWith(".html") || path.endsWith(".css")
                || path.endsWith(".ico") || path.endsWith(".svg"));
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getQueryParameter(String key) {
        return queryParameters.get(key);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getBody(String name) {
        return bodyMap.get(name);
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", method, path, protocol);
    }

}
