package webserver;

import handler.UserHandler;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.servlet.ResourceRenderer;
import webserver.http.HttpRequestParser;
import webserver.servlet.ServletManager;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String args[]) throws Exception {
        int port = 0;
        if (args == null || args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }

        // todo: 핸들러 주입 개선 (컴포넌트 스캔 스타일, 외부 파일에서 설정 고려)
        List<Object> handlers = List.of(new UserHandler());
        ResourceRenderer renderer = new ResourceRenderer();
        ServletManager servletManager = new ServletManager(renderer, handlers);
        HttpRequestParser requestParser = new HttpRequestParser();

        // 서버소켓을 생성한다. 웹서버는 기본적으로 8080번 포트를 사용한다.
        try (ServerSocket listenSocket = new ServerSocket(port);
             ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            logger.info("Web Application Server started {} port.", port);

            // 클라이언트가 연결될때까지 대기한다.
            while (true) {
                Socket connection = listenSocket.accept();
                executorService.submit(new RequestHandler(connection, servletManager,requestParser));
            }
        }
    }
}
