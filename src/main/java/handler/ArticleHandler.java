package handler;

import db.dao.ArticleDao;
import model.Article;
import model.User;
import webserver.annotation.GetMapping;
import webserver.annotation.PostMapping;
import webserver.http.HttpRequest;
import webserver.session.Session;

public class ArticleHandler {
    private final ArticleDao dao;

    public ArticleHandler(ArticleDao dao) {
        this.dao = dao;
    }

    @GetMapping("/article/create")
    public String createForm(Session session) {
        if (session.get("user") == null) {
            return "redirect:/login";
        }
        return "/article/index.html";
    }

    @PostMapping("/article/create")
    public String create(Session session, HttpRequest request) {
        User loginUser = (User) session.get("user");
        if (loginUser == null) {
            throw new IllegalStateException();
        }

        String content = request.getBody("content");
        Article created = new Article(loginUser.getId(), content);
        Long createdId = dao.save(created);

        return "redirect:/";
    }
}
