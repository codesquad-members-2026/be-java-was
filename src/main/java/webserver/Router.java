package webserver;

import fileIO.FileLoader;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import interfaces.HandlerMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);
    private final Map<String, HandlerMethod> handlers ;

    public Router(Map<String, HandlerMethod> injectedHandlers){
        this.handlers = injectedHandlers;
    }

    public void handleRequest(HttpRequest request, HttpResponse response) throws InvocationTargetException, IllegalAccessException {
        logger.info("{} requested for - {}", request.getMethod(), request.getUrl());
        String signature = extractSignature(request);
        HandlerMethod h = handlers.get(signature);
        if(h == null){
            returnStaticFiles(request,response);
            return;
        }
        h.handle(request, response);
    }

    private void returnStaticFiles(HttpRequest request, HttpResponse response) {
        if(!request.getMethod().equals("GET")){
            response.setStatus("400 Bad Request");
            logger.error("Sending 400 Bad Request Response for : {} at {}", request.getMethod(), request.getUrl());
            response.send();
        }
        String url = request.getUrl();
        try {

            byte[] file = FileLoader.getStaticFile(url);
            response.setStatus("200 OK");
            response.setHeader("Content-Type", MimeTypeParser.getContentType(MimeTypeParser.extractExtension(url)));
            response.setHeader("Content-Length", String.valueOf(file.length));
            response.setResponseBody(file);
            response.send();
        } catch (IOException e) {
            response.setStatus("404 Not Found");
            logger.error("Exception Caused 400 Bad Request Response for : {} at {}", request.getMethod(), request.getUrl());
            response.send();
        }

    }


    private String extractSignature(HttpRequest request){
        return request.getMethod().strip() + " " + request.getUrl().strip();
    }
}
