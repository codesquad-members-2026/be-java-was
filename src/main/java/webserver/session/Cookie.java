package webserver.session;

public record Cookie(String name, String value, String path, Integer maxAge) {
    public String toHeaderValue() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);

        if (path != null) {
            sb.append("; Path=").append(path);
        }

        if (maxAge != null) {
            sb.append("; Max-Age=").append(maxAge);
        }

        return sb.toString();
    }
}
