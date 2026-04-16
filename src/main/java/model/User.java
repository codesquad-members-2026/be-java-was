package model;

import db.Database;
import exception.DuplicateUserInDBException;

import java.util.Map;

public class User {
    private String userId;
    private String password;
    private String name;
    private String email;

    private User(String userId, String password, String name, String email) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public static User of(Map<String, String> bodies) {
        String tempUserId = bodies.get("userId");
        String tempUserPassword = bodies.get("password");
        String tempUserName = bodies.get("name");
        String tempUserEmail = bodies.get("email");

        if(Database.findUserById(tempUserId) == null) {
            return new User(tempUserId, tempUserPassword, tempUserName, tempUserEmail);
        }

        throw new DuplicateUserInDBException(tempUserId + " already exists");
    }

    public String getUserId() {
        return userId;
    }
    public String getPassword() {
        return password;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", password=" + password + ", name=" + name + ", email=" + email + "]";
    }
}
