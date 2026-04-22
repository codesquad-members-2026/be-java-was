package webserver.response;

import java.io.IOException;
import webserver.http.HttpResponse;
import webserver.resource.ResourceLoader;

public class TemplateRenderer implements ResponseRenderer {
    private final ResourceLoader resourceLoader;

    public TemplateRenderer(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void render(String resource, HttpResponse response, TemplateData data) throws IOException {
        String s = resourceLoader.loadAsString(resource);

        // 템플릿 엔진으로 렌더링
        response.write("");
    }
}
