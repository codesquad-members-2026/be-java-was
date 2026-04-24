package core.response;

import core.http.Mime;
import core.http.StatusCode;
import core.routing.RoutedInfo;
import core.request.HttpRequest;
import file.FileData;

import java.util.HashMap;
import java.util.Map;

public class HttpResponseBuilder {

    public static ResponseData build(RoutedInfo routedInfo, FileData fileData, HttpRequest httpRequest) {
        StatusLine statusLine = new StatusLine(httpRequest.getStartLine().protocol(), fileData.statusCode().getCode());
        Map<String, String> headers = makeHeaders(routedInfo, fileData);
        byte[] body = fileData.body();

        return new ResponseData(body, statusLine, headers);
    }

    private static Map<String, String> makeHeaders(RoutedInfo routedInfo, FileData fileData) {
        Map<String, String> headers = new HashMap<>(routedInfo.headers());
        StatusCode statusCode = fileData.statusCode();

        if(statusCode == StatusCode.FOUND) {
            headers.put("Location", routedInfo.routedPath());
        } else {
            headers.put("Content-Type", Mime.getContentTypeThroughExtension(fileData.extension()));
            headers.put("Content-Length", String.valueOf(fileData.body().length));
        }

        return headers;
    }
}
