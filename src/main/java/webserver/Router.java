package webserver;

import db.DBEntryPoint;
import fileIO.FileLoader;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import interfaces.HandlerMethod;
import model.TemplateAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.session.SessionManager;
import webserver.template.Jhymeleaf;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);
    private final Map<String, HandlerMethod> requestHandlers;
    private final SessionManager sessionManager;
    private final DBEntryPoint database;


    public Router(Map<String, HandlerMethod> injectedRequestHandlers, SessionManager sm, DBEntryPoint db){
        this.requestHandlers = injectedRequestHandlers;
        this.sessionManager = sm;
        this.database = db;
    }

    public void handleRequest(HttpRequest request, HttpResponse response) throws InvocationTargetException, IllegalAccessException, IOException, NoSuchMethodException {
        logger.info("{} requested for - {}", request.getMethod(), request.getUrl());
        String signature = extractSignature(request);
        TemplateAttributes templateAttributes = new TemplateAttributes();
        HandlerMethod h = requestHandlers.get(signature);
        if(h == null){
            returnStaticFiles(request,response);
            return;
        }
        Object result = h.handle(request, response, sessionManager, templateAttributes, database);
        if(result != null){
            response.setResponseBody(Jhymeleaf.fillTemplate((String) result, templateAttributes));
            response.setHeader("Content-Type", MimeTypeParser.MimeType.HTML.getContentType());
            response.send();
        }
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
            response.sendRedirect("/404.html");
        }

    }


    private String extractSignature(HttpRequest request){
        return request.getMethod().strip() + " " + request.getUrl().strip();
    }
}
