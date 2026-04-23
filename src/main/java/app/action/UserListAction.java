package app.action;

import db.Database;
import app.user.User;
import app.user.UserListResponseDTO;
import core.session.SessionManager;
import core.routing.RouteType;
import core.routing.RoutedInfo;
import core.request.HttpRequest;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class UserListAction implements Action {

    @Override
    public RoutedInfo process(HttpRequest httpRequest) {
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> models = new HashMap<>();

        String sessionId = httpRequest.getHeaders().getSessionId();
        if(!SessionManager.isValid(sessionId)){
            // TODO: Max-Age의 의미와 다른 방법과의 차이
            headers.put("Set-Cookie", "JSESSIONID=; Path=/; Max-Age=0");
            return RoutedInfo.of("redirect:/index.html", RouteType.REDIRECT, headers, models);
        }

        List<User> allUsers = Database.findAllUsers();
        List<UserListResponseDTO> userList = allUsers.stream().map(UserListResponseDTO::new).toList();

        StringBuilder userListHtml = new StringBuilder();
        makeUserListHtml(userList, userListHtml);

        models.put("userListHtml", userListHtml.toString());

        return RoutedInfo.of("/user/list.html", RouteType.DYNAMIC, headers, models);
    }

    private void makeUserListHtml(List<UserListResponseDTO> listDTO, StringBuilder userListHtml) {
        for(UserListResponseDTO dto : listDTO){
            userListHtml.append("<tr>")
                    .append("  <td>")
                    .append("    <div class=\"user-info\">")
                    .append("      <div class=\"user-avatar\"></div>")
                    .append("      <span class=\"user-id\">").append(dto.getUserId()).append("</span>")
                    .append("    </div>")
                    .append("  </td>")
                    .append("  <td>").append(dto.getUserName()).append("</td>")
                    .append("  <td>").append(dto.getUserEmail()).append("</td>")
                    .append("</tr>");
        }
    }
}
