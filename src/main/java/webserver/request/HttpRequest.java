package webserver.request;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest {
    private final StartLine startLine;
    private final RequestHeaders headers;
    private final Map<String, String> bodies;
    private final Map<String, String> queries;

    private static final String QUERY_SEPARATOR = "\\?";
    private static final Character KEY_VALUE_SEPARATOR = '=';
    private static final Character PARAMETER_SEPARATOR = '&';
    private static final String BLANK_SEPARATOR = " ";

    private HttpRequest(StartLine startLine, Map<String, String> headers,
                        Map<String, String> bodies, Map<String, String> queries) {
        this.startLine = startLine;
        this.headers = new RequestHeaders(headers);
        this.bodies = Map.copyOf(bodies);
        this.queries = Map.copyOf(queries);
    }

    public static HttpRequest of(InputStream in) throws IOException {
        String[] startLineSplits = readLine(in).split(BLANK_SEPARATOR);
        String method = startLineSplits[0];
        String originPath = startLineSplits[1];
        String protocol = startLineSplits[2];

        String[] pathSplits = originPath.split(QUERY_SEPARATOR);
        String path = pathSplits[0];
        Map<String, String> queries = pathSplits.length > 1 ? splitQuery(pathSplits[1]) : new HashMap<>();

        Map<String, String> headers = extractHeaders(in);
        Map<String, String> bodies = extractBody(in, headers);

        StartLine startLine = new StartLine(method, path, protocol);
        return new HttpRequest(startLine, headers, bodies, queries);
    }

    // TODO: 파서로 분리 예정
    private static String decodeValue(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
    private static Map<String, String> splitQuery(String query) {
        String[] queryParts = query.split(String.valueOf(PARAMETER_SEPARATOR));
        Map<String, String> result = new HashMap<>();

        for(String queryPart : queryParts){
            String[] keyValue = queryPart.split(String.valueOf(KEY_VALUE_SEPARATOR), 2);
            String key = keyValue[0];
            String value = keyValue.length > 1 ? decodeValue(keyValue[1]) : "";
            result.put(key, value);
        }

        return result;
    }
    private static String readLine(InputStream in) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();){
            int b;
            while((b = in.read()) != -1){
                if(b == '\n'){break;}
                if(b != '\r'){bos.write(b);}
            }
            if(bos.size() == 0 && b == -1){
                throw new EOFException("Failed to read line: Connection closed by client.");
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }
    private static Map<String, String> extractHeaders(InputStream in) throws IOException {
        Map<String, String> headers = new HashMap<>();

        String line;
        while(!(line = readLine(in)).isEmpty()){
            String[] headerSplits = line.split(":", 2);
            if(headerSplits.length == 2){
                headers.put(headerSplits[0].trim().toLowerCase(), headerSplits[1].trim());
            }
        }

        return headers;
    }
    private static Map<String, String> extractBody(InputStream in, Map<String, String> headers) throws IOException {
        int contentLength = Integer.parseInt(headers.getOrDefault("content-length", "0").trim());

        if(contentLength <= 0){
            return new HashMap<>();
        }

        byte[] body = new byte[contentLength];
        int totalRead = 0;

        while(totalRead < contentLength){
            int readCount = in.read(body, totalRead, contentLength - totalRead);

            if(readCount == -1){
                throw new EOFException("Failed to read body: Connection closed by client.");
            }

            totalRead += readCount;
        }

        // TODO: 텍스트가 아닌 파일이 오는 경우 생각
        return splitQuery(new String(body, StandardCharsets.UTF_8));
    }

    public String getStartLineInfo(){
        return startLine.printForDebug();
    }
    public String getCoreRequestInfo(){
        return getStartLineInfo() +
                headers.getOneLineHeaderInfo("host") +
                headers.getOneLineHeaderInfo("accept") +
                headers.getOneLineHeaderInfo("connection") +
                headers.getOneLineHeaderInfo("cookie");
    }
    public StartLine getStartLine(){
        return this.startLine;
    }
    public RequestHeaders getHeaders() {
        return this.headers;
    }
    public Map<String, String> getQueries() {
        return queries;
    }
    public Map<String, String> getBodies() {
        return bodies;
    }
}
