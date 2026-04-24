package webserver.response;

import java.io.IOException;
import webserver.http.HttpResponse;
import webserver.resource.ResourceLoader;
import webserver.response.template.TemplateBuilder;
import webserver.response.template.TemplateEngine;
import webserver.response.template.TemplateParser;

public class TemplateRenderer implements ResponseRenderer {
    private final ResourceLoader resourceLoader;
    private final TemplateEngine engine = new TemplateEngine(new TemplateParser(), new TemplateBuilder());

    public TemplateRenderer(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void render(String resource, HttpResponse response, TemplateData data) throws IOException {
        String template = resourceLoader.loadAsString(resource);
        String result = engine.render(template, data.toMap());
        response.write(result);
    }
}
