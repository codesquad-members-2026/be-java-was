package action;

import http.HttpRequest;

public interface Action {
    String process(HttpRequest request);
}