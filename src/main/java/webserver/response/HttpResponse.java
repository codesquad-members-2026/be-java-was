package webserver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static utils.HttpConstant.CRLF;

public class HttpResponse {
    private final String header;
    private final InputStream bodyStream;

    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private HttpResponse(String header, InputStream bodyStream) {
        this.header = header;
        this.bodyStream = bodyStream;
    }

    public static HttpResponse of(ResponseData responseData) {
        String statusCode = responseData.getStatusLine().statusCode();
        String protocol = responseData.getStatusLine().protocol();

        String header = responseHeader(responseData.getHeaders(), statusCode, protocol);
        return new HttpResponse(header, responseData.getBodyStream());
    }

    private static String responseHeader(Map<String, String> responseHeaders, String statusCode, String protocol) {
        StringBuilder result = new StringBuilder();
        result.append(protocol).append(" ").append(statusCode).append(CRLF);
        responseHeaders.forEach((key, value) -> result.append(key).append(": ").append(value).append(CRLF));
        result.append(CRLF);

        return result.toString();
    }

    public void send(DataOutputStream dos) {
        try (InputStream stream = this.bodyStream) {
            dos.writeBytes(this.header);
            stream.transferTo(dos);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
