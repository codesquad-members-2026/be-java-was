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
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RegistrationHandlers {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationHandlers.class);


    @RequestMapping(method = "GET", path = "/registration")
    public void getRegistrationPage(HttpRequest request, HttpResponse response) throws IOException{
        response.setStatus("200 OK");
        response.setHeader("Content-Type", MimeTypeParser.MimeType.HTML.getContentType());
        byte[] body = FileLoader.getStaticFile("/registration/index.html");
        response.setHeader("Content-Length",String.valueOf(body.length));
        response.setResponseBody(body);
        response.send();
    }

    @RequestMapping(method = "POST", path ="/create")
    public void postCreateUserAccount(HttpRequest request, HttpResponse response) throws IOException{

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
            User user = new User(userId,password,nickname,email);
            Database.addUser(user);
            logger.debug("NEW USER : {}",user );
            response.setStatus("302 Found");
            response.setHeader("Location","/");
            response.send();
        }

    }

}
