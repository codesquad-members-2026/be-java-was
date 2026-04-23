package app.user;

public class UserListResponseDTO {

    private String userId;
    private String userName;
    private String userEmail;

    public UserListResponseDTO(User user) {
        this.userId = user.getUserId();
        this.userName = user.getName();
        this.userEmail = user.getEmail();
    }

    public String getUserId() {
        return userId;
    }
    public String getUserName() {
        return userName;
    }
    public String getUserEmail() {
        return userEmail;
    }
}
