package handler;

import db.Database;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import model.User;
import webserver.HttpRequest;
import webserver.annotation.GetMapping;
import webserver.annotation.PostMapping;

public class UserHandler {

    @GetMapping("/")
    public String home() {
        return "/index.html";
    }

    @GetMapping("/main")
    public String mainPage() {
        return "/index.html";
    }

    @GetMapping("/registration")
    public String registerForm() {
        return "/registration/index.html";
    }

    @PostMapping("/user/create")
    public String register(HttpRequest request) {
        byte[] body = request.getBody();
        Map<String, String> keyValue = new HashMap<>();
        request.parseUrlEncodedParams(new String(body, StandardCharsets.UTF_8), keyValue);

        String userId = keyValue.get("userId");
        String password = keyValue.get("password");
        String name = keyValue.get("name");
        User newUser = new User(userId, password, name, null);

        Database.addUser(newUser);
        return "redirect:/";
    }
}
