package webserver;

import java.io.*;
import java.net.Socket;

import action.Action;
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

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {

            logger.debug("[Client's HTTP Message]");
            HttpRequest httpRequest = HttpRequest.of(br);
            String method = httpRequest.getMethod();
            String rawPath = httpRequest.getPath();
            logger.debug(httpRequest.getCoreRequestInfo());

            Action action = Router.getAction(method, rawPath);
            ResponseData responseData = (action != null)
                    ? action.process(httpRequest) : Router.convertStaticPath(rawPath);
            responseData.addProtocol(httpRequest.getProtocol());
            logger.debug("[Server's Response Data]");
            logger.debug(responseData.toString());

            HttpResponse httpResponse = HttpResponse.of(responseData);
            httpResponse.send(dos);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
