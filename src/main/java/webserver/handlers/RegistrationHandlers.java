package webserver.handlers;

import annotations.RequestMapping;
import fileIO.FileLoader;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RegistrationHandlers {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationHandlers.class);


    @RequestMapping(method = "GET", path = "/registration")
    public void getRegistrationPage(HttpRequest request, HttpResponse response) throws IOException{
        response.setStatus("200 OK");
        response.setHeader("Content-Type","text/html;charset=utf-8");
        byte[] body = FileLoader.getStaticFile("/registration/index.html");
        response.setHeader("Content-Length",String.valueOf(body.length));
        response.setResponseBody(body);
        response.send();
    }

    @RequestMapping(method = "GET", path ="/create")
    public void getCreateUserAccount(HttpRequest request, HttpResponse response) throws IOException{
        String userId = request.getParam("userID");
        String nickname = request.getParam("nickname");
        String email = request.getParam("email");
        String password = request.getParam("password");
        if(!userId.isEmpty() &&!nickname.isEmpty() &&!email.isEmpty() &&!password.isEmpty()){
            User newUser = new User(userId, password, nickname, email);
            db.Database.addUser(newUser);
            response.setStatus("302 Found");
            response.setHeader("Location","/");
            response.send();
        }
        else{
            response.setStatus("302 Found");
            response.setHeader("Location","/registration");
            response.send();
        }

    }

}
