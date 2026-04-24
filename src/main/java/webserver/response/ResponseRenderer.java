package webserver.response;

import java.io.IOException;
import webserver.http.HttpResponse;

public interface ResponseRenderer {
    void render(String resource, HttpResponse response, TemplateData data) throws IOException;
}
