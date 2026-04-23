package handler;

import db.Database;
import model.User;
import webserver.http.HttpRequest;
import webserver.annotation.GetMapping;
import webserver.annotation.PostMapping;
import webserver.response.TemplateData;
import webserver.session.Session;

public class UserHandler {

    @GetMapping("/")
    public String home(Session session, TemplateData data) {
        data.add("user", session.get("user"));
        return "/index.html";
    }

    @GetMapping("/registration")
    public String registerForm() {
        return "/registration/index.html";
    }

    @PostMapping("/user/create")
    public String register(HttpRequest request) {
        String userId = request.getBody("userId");
        String password = request.getBody("password");
        String name = request.getBody("name");
        User newUser = new User(userId, password, name, null);

        Database.addUser(newUser);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "/login/index.html";
    }

    @PostMapping("/user/login")
    public String login(HttpRequest request, Session session) {
        String userId = request.getBody("userId");
        String password = request.getBody("password");

        User user = Database.findUserById(userId);
        if (user == null || !user.getPassword().equals(password)) {
            return "/login/login_failed.html";
        }

        session.addAttribute("user", user);

        return "redirect:/";
    }

    @GetMapping("/user/list")
    public String userList(Session session, TemplateData data) {
        if (session.get("user") == null) {
            return "redirect:/login";
        }

        data.add("users", Database.findAll());
        return "/user/users.html";
    }
}
