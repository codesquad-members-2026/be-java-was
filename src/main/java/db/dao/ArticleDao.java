package db.dao;

import static db.config.DBConfig.PASSWORD;
import static db.config.DBConfig.URL;
import static db.config.DBConfig.USER;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Article;

public class ArticleDao {
    public Long save(Article article) {
        String sql = "INSERT INTO articles(user_id, content, created_at) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(
                URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, article.getUserId());
            ps.setString(2, article.getContent());
            ps.setObject(3, LocalDateTime.now(), Types.TIMESTAMP);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            throw new SQLException("ID 생성 실패");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Article> findAll() {
        String sql = "SELECT id, user_id, content, created_at FROM articles";
        List<Article> articles = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(
                URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                Long userId = rs.getLong("user_id");
                String content = rs.getString("content");
                LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
                articles.add(new Article(id, userId, content, createdAt));
            }
            return articles;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
