package action;

import db.Database;
import exception.DuplicateUserInDBException;
import webserver.response.ResponseData;
import model.User;
import webserver.request.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserCreateAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UserCreateAction.class);

    public UserCreateAction(){}

    @Override
    public ResponseData process(HttpRequest request) {
        try {
            Database.addUser(User.of(request.getBodies()));
            logger.debug("User created successfully");
            return ResponseData.of("redirect:/index.html");
        } catch (DuplicateUserInDBException de) {
            logger.error("User already exists");
            return ResponseData.of("redirect:/user/register.html");
        }
    }
}
