package handler;

import db.Database;
import java.util.Map;
import model.User;
import webserver.HttpRequest;
import webserver.annotation.Mapping;

public class UserHandler {

    @Mapping("/")
    public String home() {
        return "/index.html";
    }

    @Mapping("/main")
    public String mainPage() {
        return "/index.html";
    }

    @Mapping("/registration")
    public String registerForm() {
        return "/registration/index.html";
    }

    @Mapping("/user/create")
    public String register(HttpRequest request) {
        Map<String, String> queryParameters = request.getQueryParameters();

        String userId = queryParameters.get("userId");
        String password = queryParameters.get("password");
        String name = queryParameters.get("name");
        User newUser = new User(userId, password, name, null);

        Database.addUser(newUser);
        return "redirect:/";
    }
}
