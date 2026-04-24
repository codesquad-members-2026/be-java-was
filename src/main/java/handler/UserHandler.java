package handler;

import db.dao.UserDao;
import java.util.List;
import model.User;
import webserver.http.HttpRequest;
import webserver.annotation.GetMapping;
import webserver.annotation.PostMapping;
import webserver.response.TemplateData;
import webserver.session.Session;

public class UserHandler {
    private final UserDao dao;

    public UserHandler(UserDao dao) {
        this.dao = dao;
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
        dao.save(newUser);
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

        return dao.findByUserId(userId)
                .filter(user -> user.getPassword().equals(password))
                .map(user -> {
                    session.addAttribute("user", user);
                    return "redirect:/";
                })
                .orElse("/login/login_failed.html");
    }

    @GetMapping("/user/list")
    public String userList(Session session, TemplateData data) {
        if (session.get("user") == null) {
            return "redirect:/login";
        }
        List<User> all = dao.findAll();
        data.add("users", all);
        return "/user/users.html";
    }
}
