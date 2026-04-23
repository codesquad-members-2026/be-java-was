package app.action;

import core.routing.RoutedInfo;
import core.request.HttpRequest;

public interface Action {
    RoutedInfo process(HttpRequest request);
}