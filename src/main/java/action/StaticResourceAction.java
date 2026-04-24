package action;

import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtil;
import util.MimeType;
import util.PathUtil;

import java.io.File;
import java.io.IOException;

public class StaticResourceAction implements Action {
    private static final Logger logger = LoggerFactory.getLogger(StaticResourceAction.class);
    private static final String BASIC_PATH = "./src/main/resources/static";

    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        String path = PathUtil.normalize(request.getPath());
        File file = new File(BASIC_PATH + path);

        if (file.exists() && file.isFile()) {
            serveStaticFile(path, file, response);
            return;
        }
        serveErrorFile("404 Not Found", "/error/404.html", response);
    }

    private void serveStaticFile(String path, File file, HttpResponse response) throws IOException {
        byte[] body = FileUtil.readFile(file);
        response.addHeader("Content-Type", MimeType.getMime(path));
        response.setBody(body);
    }

    private void serveErrorFile(String status, String errorPath, HttpResponse response) throws IOException {

        File errorFile = new File(BASIC_PATH + errorPath);
        response.setStatus(status);

        if (errorFile.exists()) {
            response.addHeader("Content-Type", "text/html;charset=utf-8");
            response.setBody(FileUtil.readFile(errorFile));
        } else {
            logger.error("에러 페이지 파일을 찾을 수 없습니다: {}", errorPath);
            response.setBody("Error: " + status);
        }
    }
}