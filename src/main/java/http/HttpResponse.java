package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;

import static utils.HttpConstant.CRLF;

public class HttpResponse {
    private final String header;
    private final byte[] body;

    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private HttpResponse(String header, byte[] body) {
        this.header = header;
        this.body = body;
    }

    public static HttpResponse of(ResponseData responseData, String protocol) {
        byte[] body = responseData.getBody();
        int contentLength = body.length;
        String header = "";

        String statusCode = responseData.getHeaders().get("Status-Code");
        if(statusCode.equals(StatusCode.FOUND.getStatusCode())) { // redirect
            String location = responseData.getHeaders().get("Location");
            header = response302Header(protocol, statusCode, location, contentLength);
            return new HttpResponse(header, body);
        }

        String contentType = responseData.getHeaders().get("Content-Type");
        header = responseHeader(contentType, statusCode, protocol, contentLength);
        return new HttpResponse(header, body);
    }

    private static String responseHeader(String contentType, String statusCode, String protocol, int contentLength) {

        return protocol + " " + statusCode + CRLF +
                "Content-Type: " + contentType + CRLF +
                "Content-Length: " + contentLength + CRLF +
                CRLF;
    }

    private static String response302Header(String protocol, String statusCode, String location, int contentLength){
        return protocol + " " + statusCode + CRLF +
                "Location: " + location + CRLF +
                "Content-Length: " + contentLength + CRLF +
                CRLF;
    }

    public void send(DataOutputStream dos) {
        try {
            dos.writeBytes(this.header);
            dos.write(this.body, 0, this.body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
