package core.response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public record ResponseData (byte[] body, StatusLine statusLine, Map<String, String> headers) {

    public InputStream getBodyStream() {
        return new ByteArrayInputStream(body);
    }
}