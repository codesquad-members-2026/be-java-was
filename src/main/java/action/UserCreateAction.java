package action;

import db.Database;
import exception.DuplicateUserInDBException;
import model.User;
import http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserCreateAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UserCreateAction.class);


    public UserCreateAction(){}

    @Override
    public String process(HttpRequest request) {
        try {
            Database.addUser(User.of(request.getParameters()));
            logger.debug("User created successfully");
            return "/login/login.html";
        } catch (DuplicateUserInDBException de) {
            logger.error("User already exists");
            return "/registration/register.html";
        }
    }
}
