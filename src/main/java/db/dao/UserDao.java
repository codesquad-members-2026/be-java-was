package db.dao;

import static db.config.DBConfig.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.User;

public class UserDao {
    public Long save(User user) {
        String sql = "INSERT INTO users(userid, username, password) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(
                URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUserId());
            ps.setString(2, user.getUserName());
            ps.setString(3, user.getPassword());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            throw new SQLException("ID 생성 실패");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public List<User> findAll() {
        String sql = "SELECT id, userid, username FROM users";
        List<User> users = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(
                URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                String userId = rs.getString("userid");
                String userName = rs.getString("username");
                users.add(new User(id, userId, null, userName, null));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Optional<User> findByUserId(String userid) {
        String sql = "SELECT id, userid, password, username FROM users WHERE userid = ?";
        try (Connection conn = DriverManager.getConnection(
                URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long id = rs.getLong("id");
                    String userId = rs.getString("userId");
                    String password = rs.getString("password");
                    String userName = rs.getString("userName");
                    User u = new User(id, userId, password, userName, null);
                    return Optional.of(u);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
