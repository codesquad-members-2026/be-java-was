package webserver.servlet;

import java.io.IOException;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;
import webserver.http.Mime;
import webserver.resource.ResourceLoader;

public class StaticResourceServlet implements HttpServlet {
    private final ResourceLoader resourceLoader;

    public StaticResourceServlet(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();
        byte[] resourceBytes = resourceLoader.loadAsBytes(path);

        String extension = getExtension(path);
        response.setContentType(Mime.getMime(extension).getContentType());
        response.write(resourceBytes);
    }

    private String getExtension(String path) {
        if (path == null || path.lastIndexOf(".") == -1) {
            return "";
        }

        return path.substring(path.lastIndexOf(".") + 1);
    }
}
