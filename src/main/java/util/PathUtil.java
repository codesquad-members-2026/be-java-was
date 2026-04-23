package util;

public class PathUtil {

    private PathUtil() {}

    public static String normalize(String path) {
        if (path.equals("/")) return "/index.html";
        if (path.equals("/registration")) return "/registration/index.html";
        if (path.equals("/login")) return "/login/index.html";

        return path;
    }
}