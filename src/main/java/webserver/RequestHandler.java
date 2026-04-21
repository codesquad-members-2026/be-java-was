package webserver;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.http.HttpRequestParser;
import webserver.servlet.ServletManager;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final ServletManager servletManager;
    private final HttpRequestParser requestParser;

    public RequestHandler(Socket connectionSocket, ServletManager servletManager, HttpRequestParser requestParser) {
        this.connection = connectionSocket;
        this.servletManager = servletManager;
        this.requestParser = requestParser;
    }

    @Override
    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try {
            process();
        } catch (IOException e) {
            logger.error("요청 처리 최종 실패", e);
        }
    }

    private void process() throws IOException {
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            DataOutputStream dos = new DataOutputStream(out);
            BufferedInputStream reader = new BufferedInputStream(in);

            HttpResponse response = new HttpResponse(dos);

            try {
                HttpRequest request = requestParser.parse(reader);
                logger.debug("HTTP 요청: {}", request);
                servletManager.execute(request, response);
            } catch (Exception e) {
                logger.error("요청 처리 실패", e);
            } finally {
                response.flush();
            }
        }
    }
}
