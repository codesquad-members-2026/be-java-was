package db;

import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBEntryPoint {

    private final DBInterface dbManager;
    private static final Logger logger = LoggerFactory.getLogger(DBEntryPoint.class);
    public DBEntryPoint(DBInterface dbManager){
        this.dbManager = dbManager;
    }

    public void addUser(String userId, String password, String name, String email){
        String sql = "INSERT INTO users (USERID,PASSWORD,NAME,EMAIL) VALUES (?,?,?,?)";
        Connection conn = null;
        PreparedStatement preppedStmt = null;

        try{
            conn = dbManager.getConnection();
            preppedStmt = conn.prepareStatement(sql);
            preppedStmt.setString(1, userId);
            preppedStmt.setString(2, password);
            preppedStmt.setString(3, name);
            preppedStmt.setString(4, email);
            int affectedCount = preppedStmt.executeUpdate();
            if(affectedCount > 0){
                logger.info("Successfully added user {}", userId);
            }

        }catch (SQLException e) {
            logger.error("SQLException while executing addUser");
            throw new RuntimeException(e);
        }
        finally{
            if(preppedStmt != null){
                try{preppedStmt.close();} catch(SQLException e){ logger.error("SQLException while closing prepared statement");}
            }
            if(conn != null){
                dbManager.returnConnection(conn);
            }
        }

    }

    public User findUserById(String id){
        String sql = "SELECT * FROM users WHERE USERID = ?";
        Connection conn = null;
        PreparedStatement preppedStmt = null;
        ResultSet resultSet = null;

        try{
            conn = dbManager.getConnection();
            preppedStmt = conn.prepareStatement(sql);
            preppedStmt.setString(1, id);
            resultSet = preppedStmt.executeQuery();


            if(resultSet.next()){
                int idIdx = resultSet.getInt("ID");
                String userId = resultSet.getString("USERID");
                String password = resultSet.getString("PASSWORD");
                String username = resultSet.getString("NAME");
                String email = resultSet.getString("EMAIL");
                return  new User(idIdx, userId,password,username,email);
            }


        }catch (SQLException e) {
            logger.error("SQLException while executing findUserById");
            throw new RuntimeException(e);
        }
        finally{
            if(preppedStmt != null){
                try{preppedStmt.close();} catch(SQLException e){ logger.error("SQLException while closing prepared statement");}
            }
            if(resultSet != null){
                try{resultSet.close();} catch(SQLException e){ logger.error("SQLException while closing result set");}
            }
            if(conn != null){
                dbManager.returnConnection(conn);
            }
        }
        return null;
    }

    public Collection<User> findAllUser(){
        String sql = "SELECT * FROM users";
        Connection conn = null;
        PreparedStatement preppedStmt = null;
        ResultSet resultSet = null;
        List<User> userList = new ArrayList<>();

        try{
            conn = dbManager.getConnection();
            preppedStmt = conn.prepareStatement(sql);
            resultSet = preppedStmt.executeQuery();


            while(resultSet.next()){
                int id = resultSet.getInt("ID");
                String userId = resultSet.getString("USERID");
                String password = resultSet.getString("PASSWORD");
                String username = resultSet.getString("NAME");
                String email = resultSet.getString("EMAIL");
                User tmpUser =  new User(id, userId,password,username,email);
                userList.add(tmpUser);
            }

            return userList;

        }catch (SQLException e) {
            logger.error("SQLException while executing findAllUser");
            throw new RuntimeException(e);
        }
        finally{
            if(preppedStmt != null){
                try{preppedStmt.close();} catch(SQLException e){ logger.error("SQLException while closing prepared statement");}
            }
            if(resultSet != null){
                try{resultSet.close();} catch(SQLException e){ logger.error("SQLException while closing result set");}
            }
            if(conn != null){
                dbManager.returnConnection(conn);
            }
        }
    }

    public void addArticle(String title, String content, String authorName, int authorIdIdx, LocalDateTime currentTime){
        String sql = "INSERT INTO ARTICLES (AUTHORID,TITLE,CONTENT,CREATEDAT,USERNAME) VALUES (?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement preppedStmt = null;

        try{
            conn = dbManager.getConnection();
            preppedStmt = conn.prepareStatement(sql);
            preppedStmt.setInt(1, authorIdIdx);
            preppedStmt.setString(2, title);
            preppedStmt.setString(3, content);
            preppedStmt.setObject(4, currentTime);
            preppedStmt.setString(5, authorName);
            int affectedCount = preppedStmt.executeUpdate();
            if(affectedCount > 0){
                logger.info("Successfully added article {}", title);
            }

        }catch (SQLException e) {
            logger.error("SQLException while executing addArticle");
            throw new RuntimeException(e);
        }
        finally{
            if(preppedStmt != null){
                try{preppedStmt.close();} catch(SQLException e){ logger.error("SQLException while closing prepared statement");}
            }
            if(conn != null){
                dbManager.returnConnection(conn);
            }
        }
    }


    public Collection<Article> findAllArticles(){
        String sql = "SELECT * FROM ARTICLES";
        Connection conn = null;
        PreparedStatement preppedStmt = null;
        ResultSet resultSet = null;
        List<Article> articleList = new ArrayList<>();

        try{
            conn = dbManager.getConnection();
            preppedStmt = conn.prepareStatement(sql);
            resultSet = preppedStmt.executeQuery();


            while(resultSet.next()){
                int id = resultSet.getInt("ID");
                int authorId = resultSet.getInt("AUTHORID");
                String title = resultSet.getString("TITLE");
                String authorName = resultSet.getString("USERNAME");
                String content = resultSet.getString("CONTENT");
                Timestamp postTime = resultSet.getTimestamp("CREATEDAT");
                LocalDateTime createdAt = postTime.toLocalDateTime();
                Article newArticle = new Article(id, title,content,authorName,authorId,createdAt);
                articleList.add(newArticle);
            }

            return articleList;

        }catch (SQLException e) {
            logger.error("SQLException while executing findAllArticles");
            throw new RuntimeException(e);
        }
        finally{
            if(preppedStmt != null){
                try{preppedStmt.close();} catch(SQLException e){ logger.error("SQLException while closing prepared statement");}
            }
            if(resultSet != null){
                try{resultSet.close();} catch(SQLException e){ logger.error("SQLException while closing result set");}
            }
            if(conn != null){
                dbManager.returnConnection(conn);
            }
        }
    }

}
