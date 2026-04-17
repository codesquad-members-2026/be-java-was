package webserver.handlers;

import annotations.RequestMapping;
import db.Database;
import fileIO.FileLoader;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.TemplateAttributes;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.session.Session;
import webserver.session.SessionManager;


import java.io.IOException;

import static auth.JUserAuth.checkPassword;


public class LoginHandlers {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandlers.class);

    @RequestMapping(method = "GET", path ="/login")
    public String getLoginPage(HttpResponse response) throws IOException {
        response.sendHtml("/login/index.html");
        return null;
    }

    @RequestMapping(method = "POST", path ="/login")
    public String postLoginRequest(HttpRequest request, HttpResponse response, SessionManager sessionManager) throws IOException {
        String id = request.getBodyParam("userID");
        String password = request.getBodyParam("password");
        User possibleUser = Database.findUserById(id);

        if(id.isEmpty() || password.isEmpty() || possibleUser == null){
            response.sendRedirect("/login/failed.html");
            return null;
        }

        if(!checkPassword(possibleUser.getPassword(), password)){
            response.sendRedirect("/login/failed.html");
            return null;
        }

        Session newSession = sessionManager.createNewSession();
        logger.info("New Session in Memory  SID = {}", newSession.getId());
        newSession.addAttribute("user",possibleUser);
        response.setHeader("Set-Cookie", "SID="+newSession.getId()+";"+" Path=/; Max-Age=300; HttpOnly");
        response.setStatus("302 Found");
        response.setHeader("location","/user/list");
        response.send();
        return null;
    }

}
