package db.config;

import static db.config.DBConfig.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBInitializer {
    public static void init() {
        try (Connection conn = DriverManager.getConnection(
                URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String sql = new String(DBInitializer.class
                    .getResourceAsStream("/schema.sql")
                    .readAllBytes());
            stmt.execute(sql);
            System.out.println("DB 초기화 완료");
        } catch (Exception e) {
            throw new RuntimeException("DB 초기화 실패", e);
        }
    }
}
