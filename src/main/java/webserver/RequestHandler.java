package webserver;

import java.io.*;
import java.net.Socket;

import webserver.request.HttpRequest;
import webserver.response.HttpResponse;
import webserver.response.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}",
                connection.getInetAddress(), connection.getPort());
        
        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream()) {
            logger.debug("[Client's HTTP Message]");

            HttpRequest httpRequest = HttpRequest.of(in);
            logger.debug(httpRequest.getCoreRequestInfo());

            ResponseData responseData = Router.makeResponseData(httpRequest);
            logger.debug("[Server's Response Data]");
            logger.debug(responseData.toString());

            HttpResponse httpResponse = HttpResponse.of(responseData);
            httpResponse.send(out);

        } catch (EOFException e){
            logger.debug("Client disconnected: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("I/O error occurred", e);
        }
    }
}
