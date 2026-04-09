package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String protocol;
    private final Map<String, String> headers;

    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private static final String CRLF = "\r\n";

    private HttpRequest(String method, String path, String protocol, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = headers;
    }

    // TODO: 왜 정적 팩토리 메서드로 객체를 반환하는가? 파싱 작업이 많이 일어나기 때문? -> 생성자에서 복잡한 연산이 이루어져서는 안됌
    public static HttpRequest of(BufferedReader br) throws IOException {
        List<String> startLine = Arrays.stream(br.readLine().split(" ")).toList();
        String method = startLine.get(0);
        String path = startLine.get(1);
        String protocol = startLine.get(2);

        Map<String, String> headers = new HashMap<>();
        String line;
        while((line = br.readLine()) != null) {
            if(line.isEmpty())
                break;

            String[] header = line.split(":", 2);
            headers.put(header[0].trim(), header[1].trim());
        }

        return new HttpRequest(method, path, protocol, headers);
    }

    public String getCoreRequestInfo(){
        StringBuilder coreRequestInfo = new StringBuilder();
        coreRequestInfo.append(method).append(" ").append(path).append(" ").append(protocol).append(" ").append(CRLF);
        headers.forEach((k, v) -> {
            if(k.equals("Host") || k.equals("Accept") || k.equals("Connection")){
                coreRequestInfo.append(k).append(": ").append(v).append(CRLF);
            }
        });

        return coreRequestInfo.toString();
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
}
