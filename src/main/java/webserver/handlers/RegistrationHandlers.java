package webserver.handlers;

import annotations.RequestMapping;
import db.DBEntryPoint;
import db.Database;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static auth.JUserAuth.hashPassword;

public class RegistrationHandlers {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationHandlers.class);


    @RequestMapping(method = "GET", path = "/registration")
    public void getRegistrationPage(HttpResponse response) throws IOException{
        response.sendHtml("/registration/index.html");
    }

    @RequestMapping(method = "POST", path ="/create")
    public void postCreateUserAccount(HttpRequest request, HttpResponse response, DBEntryPoint database){

        String userId = request.getBodyParam("userID");
        String nickname = request.getBodyParam("nickname");
        String email = request.getBodyParam("email");
        String password = request.getBodyParam("password");

        if(userId==null || nickname==null || email==null|| password==null){
            response.sendRedirect("/registration");
        }
        else{
            String hashedPW = hashPassword(password);
            database.addUser(userId, hashedPW, nickname, email);
            logger.info("NEW USER CREATED : {}", userId );
            response.sendRedirect("/");
        }

    }

}
