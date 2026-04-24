package webserver.response;

import webserver.http.HttpResponse;

public class RedirectRenderer implements ResponseRenderer{
    @Override
    public void render(String resource, HttpResponse response, TemplateData data) {
        String location = resource.substring("redirect:".length());
        response.sendRedirect(location);
    }
}
