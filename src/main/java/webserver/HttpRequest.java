package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String protocol;
    private final Map<String, String> header;

    private HttpRequest(String method, String path, String protocol, Map<String, String> header) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.header = header;
    }

    public static HttpRequest from(BufferedReader br) throws IOException {
        StringTokenizer tokenizer = new StringTokenizer(br.readLine());
        String method = tokenizer.nextToken();
        String path = tokenizer.nextToken();
        String protocol = tokenizer.nextToken();

        // header 읽기
        String line;
        Map<String, String> header = new HashMap<>();
        while (!(line = br.readLine()).isEmpty()) {
            String[] parts = line.split(":", 2);
            String key = parts[0].trim();
            String value = parts[1].trim();
            header.put(key, value);
        }

        // TODO: body 읽기
        //int contentLength = Integer.parseInt(header.get("Content-Length"));

        return new HttpRequest(method, path, protocol, Map.copyOf(header));
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

    public Map<String, String> getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", method, path, protocol);
    }

}
