package webserver.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.Mime;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class StaticResourceServlet implements HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(StaticResourceServlet.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();

        File file = new File("src/main/resources/static" + path);

        byte[] body = null;

        if (file.exists() && file.isFile()) {
            // todo: nio 사용하지 않기
            body = Files.readAllBytes(file.toPath());
            logger.debug("success to read: {}", request.getPath());
        }

        String extension = getExtension(path);
        response.setContentType(Mime.getMime(extension).getContentType());
        response.write(body);
    }

    private void read(String path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path)) {
            byte[] bytes = fis.readAllBytes();

        }
    }
    private static String getExtension(String path) {
        if (path == null || path.lastIndexOf(".") == -1) {
            return  "";
        }

        return path.substring(path.lastIndexOf(".") + 1);
    }
}
