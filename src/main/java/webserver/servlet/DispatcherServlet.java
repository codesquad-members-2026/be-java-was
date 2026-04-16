package webserver.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.PageNotFoundException;

public class DispatcherServlet implements HttpServlet {
    private final Map<String,Object> pathMap;

    public DispatcherServlet(List<Object> handlers) {
        this.pathMap = new HashMap<>();
        initialize(handlers);
    }

    private void initialize(List<Object> handlers) {
        // 핸들러 어노테이션 스캔
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();
        // todo: 디버그 후에 지우기
        response.write(path + "로 요청이 왔어요");

//        Object action = pathMap.get(path);
//        if (action == null) {
//            // 정적 리소스 시도
//            // 디스패쳐 서블릿
//            throw new PageNotFoundException("요청 주소 = " + path);
//        }
//        // 여기서 invoke로 실행
    }
}
