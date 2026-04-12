package webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = getPort(args);

        // TODO: 해당 구조를 잡아야 하는 이유
        try (ServerSocket listenSocket = new ServerSocket(port);
             ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            logger.info("Web Application Server started {} port.", port);

            while (true) {
                try {
                    Socket connection = listenSocket.accept();

                    executor.execute(new RequestHandler(connection));

                } catch (IOException ie) {
                    logger.error("소켓 연결 수락 중 에러 발생 (서버 계속 동작): {}", ie.getMessage());
                }
            }
        } catch (IOException ex) {
            logger.error("서버 부팅 실패 (치명적 에러): {}", ex.getMessage());
        }
    }

    private static int getPort(String[] args) {
        return args == null || args.length == 0 ? DEFAULT_PORT : Integer.parseInt(args[0]);
    }
}
