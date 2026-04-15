package webserver;

import java.io.*;
import java.net.Socket;
import action.Action;
import action.ActionMap;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.PathUtil;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            String path = request.getPath();
            int index = path.indexOf("?");

            if(index != -1) {
                path = path.substring(0, index);
            }

            Action action = ActionMap.getAction(path);

            if (action != null) {
                action.execute(request, response);
                return;
            }

            path = PathUtil.normalize(path);
            response.serveFile(path);

        } catch (IOException e) {
            logger.error("요청 처리 중 에러 발생: {}", e.getMessage());
        }
    }
}