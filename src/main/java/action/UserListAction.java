package action;

import db.Database;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public class UserListAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(UserListAction.class);

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        User user = getSessionUser(request);
        if (user == null) {
            logger.debug("로그인이 되어 있지 않습니다. 로그인 페이지로 이동합니다.");
            response.sendRedirect("/login/index.html");
            return;
        }

        Collection<User> userList = Database.findAll();
        StringBuilder sb = new StringBuilder();
        int count = 1;

        for (User u : userList) {
            sb.append("<tr>")
                    .append("<td><span class='badge-no'>").append(count++).append("</span></td>")
                    .append("<td>").append(u.getUserId()).append("</td>")
                    .append("<td>").append(u.getName()).append("</td>")
                    .append("<td>").append(u.getEmail()).append("</td>")
                    .append("</tr>\n");
        }

        String layoutHtml = renderWithLayout("/user/list.html", user);
        String finalHtml = layoutHtml.replace("{{userList}}", sb.toString());

        response.setBody(finalHtml);
    }
}