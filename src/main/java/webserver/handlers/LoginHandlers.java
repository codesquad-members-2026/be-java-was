package webserver.handlers;

import annotations.RequestMapping;
import db.Database;
import fileIO.FileLoader;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.MimeTypeParser;

import java.io.IOException;

import static auth.JUserAuth.hashPassword;

public class LoginHandlers {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandlers.class);

    @RequestMapping(method = "GET", path ="/login")
    public void getLoginPage(HttpRequest request, HttpResponse response) throws IOException {

        response.setStatus("200 OK");
        response.setHeader("Content-Type", MimeTypeParser.MimeType.HTML.getContentType());
        byte[] body = FileLoader.getStaticFile("/login/index.html");
        response.setHeader("Content-Length",String.valueOf(body.length));
        response.setResponseBody(body);
        response.send();

    }

    @RequestMapping(method = "POST", path ="/login")
    public void postLoginRequest(HttpRequest request, HttpResponse response) throws IOException {

        logger.info("User Login - User : {}", request.getBodyParam("userID"));

    }

}
