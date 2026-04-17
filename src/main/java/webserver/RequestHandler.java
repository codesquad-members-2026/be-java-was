package webserver;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import jhttp.HttpRequest;
import jhttp.HttpRequestParser;
import jhttp.HttpResponse;
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
        logger.info("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream()) {

            HttpRequest newRequest = HttpRequestParser.parse(in);
            HttpResponse newResponse = new HttpResponse(out);
            if (newRequest == null) return;
            try {
                router.handleRequest(newRequest, newResponse);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}

