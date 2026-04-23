package webserver.response;

import webserver.resource.ResourceLoader;

public class ResponseResolver {
    private final RedirectRenderer redirectRenderer;
    private final TemplateRenderer templateRenderer;

    public ResponseResolver(ResourceLoader resourceLoader) {
        this.templateRenderer = new TemplateRenderer(resourceLoader);
        this.redirectRenderer = new RedirectRenderer();
    }

    public ResponseRenderer resolve(String resource) {
        if (resource.startsWith("redirect:")) {
            return redirectRenderer;
        } else {
            return templateRenderer;
        }
    }
}
