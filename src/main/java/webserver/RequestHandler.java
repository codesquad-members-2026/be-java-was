package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final ServletManager servletManager;

    public RequestHandler(Socket connectionSocket, ServletManager servletManager) {
        this.connection = connectionSocket;
        this.servletManager = servletManager;
    }

    @Override
    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try {
            process();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void process() throws IOException {
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            DataOutputStream dos = new DataOutputStream(out);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            HttpRequest request = HttpRequest.from(br);
            HttpResponse response = new HttpResponse(dos);

            logger.debug("HTTP 요청: {}", request);
            servletManager.execute(request, response);
            response.flush();
            logger.debug("HTTP 응답 완료");
        }
    }
}
