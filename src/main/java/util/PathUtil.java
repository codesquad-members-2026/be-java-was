package util;

public class PathUtil {

    private PathUtil() {}

    public static String normalize(String path) {
        // [분석] 기본 경로들을 구체적인 HTML 파일로 연결해주는 웰컴 페이지 매핑 로직입니다.
        if (path.equals("/")) return "/index.html";
        if (path.equals("/registration")) return "/registration/index.html";
        if (path.equals("/login")) return "/login/index.html";

        // TODO: ".."이나 "//" 같이 비정상적이거나 보안에 위협이 되는 문자가 포함되어 있는지
        // 검사하고 정제하는 로직을 추가해 보세요. (Path Traversal 방어)

        // TODO: 만약 확장자가 없는 주소(예: /user/list)가 들어왔을 때,
        // 자동으로 뒤에 ".html"을 붙여주거나 특정 규칙에 따라 파일을 찾게 하는 공통 로직을 고민해 보세요.

        return path;
    }
}