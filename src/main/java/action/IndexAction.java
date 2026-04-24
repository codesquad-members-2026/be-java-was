package action;

import http.HttpRequest;
import http.HttpResponse;
import model.User;
import java.io.IOException;


public class IndexAction extends AbstractAction {

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        User user = getSessionUser(request);
        String finalHtml = renderWithLayout("/index.html", user);
        response.setBody(finalHtml);
    }
}