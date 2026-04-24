package handler;

import db.dao.ArticleDao;
import db.dao.UserDao;
import java.util.List;
import model.Article;
import webserver.annotation.GetMapping;
import webserver.response.TemplateData;
import webserver.session.Session;

public class HomeHandler {
    private final UserDao userDao;
    private final ArticleDao articleDao;

    public HomeHandler(UserDao userDao, ArticleDao articleDao) {
        this.userDao = userDao;
        this.articleDao = articleDao;
    }

    @GetMapping("/")
    public String home(Session session, TemplateData data) {
        data.add("user", session.get("user"));
        List<Article> all = articleDao.findAll();
        for (Article article : all) {
            System.out.println(article);
        }
        data.add("articles", all);
        return "/index.html";
    }
}
