package webserver.handlers;

import annotations.RequestMapping;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;

public class MainPageHandlers {
    private static final Logger logger = LoggerFactory.getLogger(MainPageHandlers.class);

    @RequestMapping(method = "GET", path = "/")
    public void getFrontPage(HttpResponse response) throws IOException {
        response.sendHtml("/index.html");
    }

}
