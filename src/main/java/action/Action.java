package action;

import http.HttpRequest;
import http.ResponseData;

public interface Action {
    ResponseData process(HttpRequest request);
}