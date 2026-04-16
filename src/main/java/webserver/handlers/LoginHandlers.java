package webserver.handlers;

import annotations.RequestMapping;
import db.Database;
import fileIO.FileLoader;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.session.Session;


import java.io.IOException;

import static auth.JUserAuth.checkPassword;
import static auth.JUserAuth.hashPassword;

public class LoginHandlers {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandlers.class);

    @RequestMapping(method = "GET", path ="/login")
    public void getLoginPage(HttpResponse response) throws IOException {
        response.sendHtml("/login/index.html");
    }

    @RequestMapping(method = "POST", path ="/login")
    public void postLoginRequest(HttpRequest request, HttpResponse response) throws IOException {
        String id = request.getBodyParam("userID");
        String password = request.getBodyParam("password");
        User possibleUser = Database.findUserById(id);

        if(id.isEmpty() || password.isEmpty() || possibleUser == null){
            response.sendRedirect("/login/index.html");
            return;
        }

        if(!checkPassword(possibleUser.getPassword(), password)){
            response.sendRedirect("/login/index.html");
            return;
        }




    }

}
