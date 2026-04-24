package webserver.response.template;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import webserver.response.template.Token.SectionEnd;
import webserver.response.template.Token.SectionStart;
import webserver.response.template.Token.Text;
import webserver.response.template.Token.Variable;

public class TemplateParser {
    private static final Pattern TAG = Pattern.compile("\\{\\{([#^/]?)\\s*([\\w.]+)\\s*\\}\\}");

    public List<Token> parse(String template) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = TAG.matcher(template);
        int lastIdx = 0;

        while (matcher.find()) {
            if (matcher.start() > lastIdx) {
                tokens.add(new Text(template.substring(lastIdx, matcher.start())));
            }
            tokens.add(getToken(matcher.group(1), matcher.group(2)));
            lastIdx = matcher.end();
        }

        if (lastIdx < template.length()) {
            tokens.add(new Text(template.substring(lastIdx)));
        }
        return tokens;
    }

    private Token getToken(String sigil, String key) {
        return switch (sigil) {
            case "#" -> new SectionStart(key, false);
            case "^" -> new SectionStart(key, true);
            case "/" -> new SectionEnd(key);
            case "" -> new Variable(key);
            default -> throw new IllegalArgumentException("템플릿 문법 오류");
        };
    }

}

