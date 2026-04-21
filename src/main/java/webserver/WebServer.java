package webserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import db.CodestagramDBManager;
import db.DBEntryPoint;
import interfaces.HandlerMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.scanner.ComponentScannerWithoutGemini;
import webserver.session.SessionManager;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;
//    private static final ExecutorService threadPool = Executors.newFixedThreadPool(2000);
    private static final ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();

    // Virtual Thread does not dramatically increase a single request
    // However it increases the ThroughPut of requests because IO bound blocking does not affect other requests



    public static void main(String args[]) throws Exception {
//        Map<String, HandlerMethod> handlerMethodMap = ComponentScanner.scanHandlers("webserver.handlers");
        Map<String, HandlerMethod> handlerMethodMap = ComponentScannerWithoutGemini.loadHandlers("webserver/handlers");
        SessionManager sessionManager = new SessionManager();
        CodestagramDBManager dbManager = new CodestagramDBManager(30);
        DBEntryPoint database = new DBEntryPoint(dbManager);
        Router router = new Router(handlerMethodMap, sessionManager, database);

        int port = 0;
        if (args == null || args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }

        // 서버소켓을 생성한다. 웹서버는 기본적으로 8080번 포트를 사용한다.
        try (ServerSocket listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);
            // 클라이언트가 연결될때까지 대기한다.
            Socket connection;
            while ((connection = listenSocket.accept()) != null) {
                connection.setSoTimeout(10000);
                threadPool.execute(new RequestHandler(connection, router));
            }
        }
    }
}
