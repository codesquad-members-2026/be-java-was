package webserver.handlers;

import annotations.RequestMapping;
import fileIO.FileLoader;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.MimeTypeParser;

import java.io.IOException;

public class MainPageHandlers {
    private static final Logger logger = LoggerFactory.getLogger(MainPageHandlers.class);
    @RequestMapping(method = "GET", path = "/")
    public void getFrontPage(HttpRequest request, HttpResponse response) throws IOException {
        response.setStatus("200 OK");
        response.setHeader("Content-Type", MimeTypeParser.MimeType.HTML.getContentType());
        byte[] body = FileLoader.getStaticFile("/index.html");
        response.setHeader("Content-Length",String.valueOf(body.length));
        response.setResponseBody(body);
        response.send();
    }
}
