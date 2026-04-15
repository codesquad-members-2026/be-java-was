package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;


    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            HttpRequest request = new HttpRequest(in);
            String path = request.getPath();

            if (path.equals("/")) {
                path = "/index.html";
            }

            if(path.equals("/registration")) {
                path = "/registration/index.html";
            }

            if (path.equals("/user/create")) {

                String userId = request.getParameter("userId");
                String password = request.getParameter("password");
                String name = request.getParameter("name");
                String email = request.getParameter("email");

                User user = new User(userId, password, name, email);

                Database.addUser(user);
                logger.debug("회원가입 성공: {}", user);

                HttpResponse response = new HttpResponse(out);
                response.sendRedirect("/index.html");
                return;
            }

            byte[] body = Files.readAllBytes(Paths.get("./src/main/resources/static" + path));

            HttpResponse response = new HttpResponse(out);
            response.forward(path, body);

        } catch (IOException e) {
            logger.error("요청 처리 중 예외 발생: {}", e.getMessage());
        }
    }
}

