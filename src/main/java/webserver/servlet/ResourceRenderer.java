package webserver.servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.HttpResponse;
import webserver.http.Mime;
import webserver.exception.PageNotFoundException;

public class ResourceRenderer {
    private static final Logger logger = LoggerFactory.getLogger(ResourceRenderer.class);

    public void render(String resource, HttpResponse response) throws IOException {
        if (resource.startsWith("redirect:")) {
            String location = resource.substring("redirect:".length());
            response.sendRedirect(location);
            return;
        }

        File file = new File("src/main/resources/static" + resource);

        byte[] body = null;

        if (!file.exists() || !file.isFile()) {
            throw new PageNotFoundException("리소스를 찾을 수 없음");
        }

        // todo: nio 사용하지 않기
        body = Files.readAllBytes(file.toPath());

        String extension = getExtension(resource);
        response.setContentType(Mime.getMime(extension).getContentType());
        response.write(body);
    }

    private static String getExtension(String path) {
        if (path == null || path.lastIndexOf(".") == -1) {
            return  "";
        }

        return path.substring(path.lastIndexOf(".") + 1);
    }
}
