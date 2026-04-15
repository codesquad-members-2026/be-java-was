package webserver.handlers;

import annotations.RequestMapping;
import db.Database;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static auth.JUserAuth.hashPassword;

public class LoginHandlers {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandlers.class);

    @RequestMapping(method = "GET", path ="/login")
    public void postCreateUserAccount(HttpRequest request, HttpResponse response) throws IOException {

        String userId = request.getBodyParam("userID");
        String nickname = request.getBodyParam("nickname");
        String email = request.getBodyParam("email");
        String password = request.getBodyParam("password");

        if(userId==null || nickname==null || email==null|| password==null){
            response.setStatus("302 Found");
            response.setHeader("Location","/registration");
            response.send();
        }
        else{
            String hashedPW = hashPassword(password);
            User user = new User(userId,hashedPW,nickname,email);
            Database.addUser(user);
            logger.debug("NEW USER : {}",user );
            response.setStatus("302 Found");
            response.setHeader("Location","/");
            response.send();
        }

    }

}
