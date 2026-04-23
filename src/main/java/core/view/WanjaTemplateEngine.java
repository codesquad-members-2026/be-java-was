package core.view;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WanjaTemplateEngine {

    // TODO: Pattern 이란
    private static final Pattern PATTERN = Pattern.compile("\\{\\{\\s*(.*?)\\s*\\}\\}");

    public static byte[] convertFile(byte[] originContent, Map<String, Object> model) throws IOException {
        String htmlString = new String(originContent, StandardCharsets.UTF_8);

        // TODO: String.replace()과 비교
        Matcher matcher = PATTERN.matcher(htmlString);
        StringBuilder renderedHtml = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = model.get(key);
            String replacement = (value != null) ? value.toString() : "";
            matcher.appendReplacement(renderedHtml, replacement);
        }

        // TODO: 잘 이해안됌
        matcher.appendTail(renderedHtml);

        return renderedHtml.toString().getBytes(StandardCharsets.UTF_8);
    }
}