package app.action;

import app.user.UserListResponse;
import db.Database;
import app.user.User;
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
        List<UserListResponse> userList = allUsers.stream()
                .map(u -> new UserListResponse(u.getUserId(), u.getName(), u.getEmail()))
                .toList();

        StringBuilder userListHtml = new StringBuilder();
        makeUserListHtml(userList, userListHtml);

        models.put("userListHtml", userListHtml.toString());

        return RoutedInfo.of("/user/list.html", RouteType.DYNAMIC, headers, models);
    }

    // TODO: 자바 코드 안에 뷰(HTML) 코드가 섞여 있음 --> 템플릿 엔진이 반복문을 구현하면, 유저의 리스트 데이터만 보내기
    private void makeUserListHtml(List<UserListResponse> userList, StringBuilder userListHtml) {
        for(UserListResponse user : userList){
            userListHtml.append("<tr>")
                    .append("  <td>")
                    .append("    <div class=\"user-info\">")
                    .append("      <div class=\"user-avatar\"></div>")
                    .append("      <span class=\"user-id\">").append(user.userId()).append("</span>")
                    .append("    </div>")
                    .append("  </td>")
                    .append("  <td>").append(user.userName()).append("</td>")
                    .append("  <td>").append(user.userEmail()).append("</td>")
                    .append("</tr>");
        }
    }
}
