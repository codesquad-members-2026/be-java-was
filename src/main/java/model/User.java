package model;

public class User {
    private Long id;
    private String userId;
    private String password;
    private String userName;
    private String email;

    public User(Long id, String userId, String password, String userName, String email) {
        this.id = id;
        this.userId = userId;
        this.password = password;
        this.userName = userName;
        this.email = email;
    }

    public User(String userId, String password, String userName, String email) {
        this(null, userId, password, userName, email);
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User [id =" + id
                + "userId=" + userId + ", password=" + password + ", name=" + userName + ", email=" + email + "]";
    }
}
