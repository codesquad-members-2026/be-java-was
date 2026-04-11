package webserver;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import http.HttpRequest;
import http.HttpRequestParser;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private Router router;

    public RequestHandler(Socket connectionSocket, Router r) {
        this.connection = connectionSocket;
        this.router =r;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream()) {

            HttpRequest newRequest = HttpRequestParser.parse(in);
            HttpResponse newResponse = new HttpResponse(out);
            logger.debug("MyHttpRequest is null? : {}", newRequest == null);
            if (newRequest == null) return;
            try {
                router.handleRequest(newRequest, newResponse);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            logger.debug("IO EXCEPTION POPPED");
            logger.error(e.getMessage());
        }
    }
}

