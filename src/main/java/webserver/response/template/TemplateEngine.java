package webserver.response.template;

import java.util.List;
import webserver.response.TemplateData;

public class TemplateEngine {
    private final TemplateParser parser;
    private final TemplateBuilder builder;

    public TemplateEngine(TemplateParser parser, TemplateBuilder builder) {
        this.parser = parser;
        this.builder = builder;
    }

    public String render(String template, Object data) {
        List<Token> tokens = parser.parse(template);
        StringBuilder sb = new StringBuilder();
        builder.build(tokens, 0, tokens.size(), data, sb);
        return sb.toString();
    }
}
