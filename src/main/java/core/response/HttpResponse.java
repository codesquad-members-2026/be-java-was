package core.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static core.http.HttpConstant.CRLF;

public class HttpResponse {
    private final String header;
    private final InputStream bodyStream;

    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private HttpResponse(String header, InputStream bodyStream) {
        this.header = header;
        this.bodyStream = bodyStream;
    }

    public static HttpResponse of(ResponseData responseData) {
        String header = responseHeader(responseData.headers(), responseData.statusLine());

        // TODO: 왜 getBodyStream()을 이용하여 InputStream 자료형으로 저장하는가?
        return new HttpResponse(header, responseData.getBodyStream());
    }

    private static String responseHeader(Map<String, String> responseHeaders, StatusLine statusLine) {
        StringBuilder result = new StringBuilder();
        result.append(statusLine.protocol()).append(" ").append(statusLine.statusCode()).append(CRLF);
        responseHeaders.forEach((key, value) -> result.append(key).append(": ").append(value).append(CRLF));
        result.append(CRLF);

        return result.toString();
    }

    public void send(OutputStream out) {
        try {
            out.write(this.header.getBytes(StandardCharsets.UTF_8));

            if(this.bodyStream != null) {
                try (InputStream stream = this.bodyStream) {
                    stream.transferTo(out);
                }
            }

            out.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
