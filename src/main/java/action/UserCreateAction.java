package action;

import db.Database;
import exception.DuplicateUserInDBException;
import model.User;
import http.HttpRequest;

public class UserCreateAction implements Action {

    public UserCreateAction(){}

    @Override
    public String process(HttpRequest request) {
        try {
            Database.addUser(User.of(request.getParameters()));
            return "/login/login.html";
        } catch (DuplicateUserInDBException de) {
            return "/registration/register.html";
        }
    }
}
