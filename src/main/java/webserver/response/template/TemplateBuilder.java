package webserver.response.template;

import static webserver.response.template.ContextLookup.*;

import java.util.List;
import webserver.response.template.Token.*;

public class TemplateBuilder {
    public void build(List<Token> tokens, int start, int end, Object context, StringBuilder sb) {
        int i = start;
        while (i < end) {
            Token token = tokens.get(i);

            switch (token) {
                case Text text -> {
                    sb.append(text.context());
                    i++;
                }
                case Variable variable -> {
                    Object lookup = lookup(variable.key(), context);
                    if (lookup != null) {
                        sb.append(lookup);
                    }
                    i++;
                }
                case SectionStart section-> {
                    int sectionEndIdx = findSectionEnd(tokens, i + 1, section.key());
                    Object sectionContext = lookup(section.key(), context);

                    if (section.inverted()) {
                        if (isFalsy(sectionContext)) {
                            build(tokens, i + 1, sectionEndIdx, sectionContext, sb);
                        }
                    } else {
                        if (sectionContext instanceof Iterable<?> iter) {
                            for (Object item : iter) {
                                build(tokens, i + 1, sectionEndIdx, item, sb);
                            }
                        } else if (!isFalsy(sectionContext)) {
                            Object newContext = (sectionContext instanceof Boolean) ? context : sectionContext;
                            build(tokens, i + 1, sectionEndIdx, newContext, sb);
                        }
                    }
                    i = sectionEndIdx + 1;
                }
                case SectionEnd sectionEnd -> {
                    throw new IllegalStateException(
                            "짝이 맞지 않는 섹션 종료 태그. token key=" + sectionEnd.key() + "token index=" + i);
                }
            }
        }
    }

    public int findSectionEnd(List<Token> tokens, int from, String key) {
        int depth = 0;
        for (int i = from; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token instanceof SectionStart start && start.key().equals(key)) {
                depth++;
            } else if (token instanceof SectionEnd end && end.key().equals(key)) {
                if (depth == 0) {
                    return i;
                }
                depth--;
            }
        }
        throw new IllegalStateException("섹션이 닫히지 않음: " + key);
    }
}
