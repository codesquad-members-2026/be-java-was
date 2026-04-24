package app.action;

import db.Database;
import exception.DuplicateUserInDBException;
import app.user.User;
import core.routing.RouteType;
import core.routing.RoutedInfo;
import core.request.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class UserCreateAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UserCreateAction.class);

    public UserCreateAction(){}

    @Override
    public RoutedInfo process(HttpRequest request) {
        String routingPath;
        try {
            Database.addUser(User.of(request.getBodies()));
            logger.debug("User created successfully");
            routingPath = "redirect:/index.html";
            return RoutedInfo.of(routingPath, RouteType.REDIRECT, new HashMap<>(), new HashMap<>());
        } catch (DuplicateUserInDBException de) {
            logger.error("User already exists");
            routingPath = "redirect:/user/register.html";
            return RoutedInfo.of(routingPath, RouteType.REDIRECT, new HashMap<>(), new HashMap<>());
        }
    }
}
