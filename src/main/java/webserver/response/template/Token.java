package webserver.response.template;

public sealed interface Token {
    record Text(String context) implements Token {
    }

    record Variable(String key) implements Token {
    }

    record SectionStart(String key, boolean inverted) implements Token {
    }

    record SectionEnd(String key) implements Token {
    }
}
