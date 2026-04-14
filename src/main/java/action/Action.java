package action;

import webserver.request.HttpRequest;
import webserver.response.ResponseData;

public interface Action {
    ResponseData process(HttpRequest request);
}