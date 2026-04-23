package action;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

public interface Action {
    void execute(HttpRequest request, HttpResponse response) throws IOException;
}