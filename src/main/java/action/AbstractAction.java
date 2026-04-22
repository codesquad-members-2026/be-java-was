package action;

import http.HttpRequest;
import http.HttpResponse;
import model.User;
import session.SessionManager;
import util.FileUtil;

import java.io.IOException;

public abstract class AbstractAction implements Action {

    protected static final String BASIC_PATH = "./src/main/resources/static";

    @Override
    public abstract void execute(HttpRequest request, HttpResponse response) throws IOException;

    protected String readResource(String path) throws IOException {
        byte[] buffer = FileUtil.readFile(new java.io.File(BASIC_PATH + path));
        return new String(buffer, "UTF-8");
    }

    protected User getSessionUser(HttpRequest request) {
        String sid = request.getCookie("SID");
        if (sid == null) return null;
        return SessionManager.getInstance().read(sid);
    }

    protected String renderWithLayout(String templatePath, User user) throws IOException {
        String mainHtml = readResource(templatePath);

        String fragmentPath = (user == null) ? "/fragments/nav_guest.html" : "/fragments/nav_user.html";
        String menuHtml = readResource(fragmentPath);

        if (user != null) {
            menuHtml = menuHtml.replace("{{userName}}", user.getName());
        }

        return mainHtml.replace("{{header_items}}", menuHtml);
    }
}