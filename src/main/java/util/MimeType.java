package util;

public enum MimeType {

    HTML(".html", "text/html"),
    CSS(".css", "text/css"),
    JS(".js", "text/javascript"),
    SVG(".svg", "image/svg+xml"),
    PNG(".png", "image/png"),
    ICO(".ico", "image/x-icon"),

    UNKNOWN("", "application/octet-stream");

    private final String extension;
    private final String mime;

    MimeType(String extension, String mime) {
        this.extension = extension;
        this.mime = mime;
    }

    public static String getMime(String path) {

        String lowerPath = path.toLowerCase();

        return java.util.Arrays.stream(values())
                .filter(m -> !m.extension.isEmpty())
                .filter(m -> lowerPath.endsWith(m.extension))
                .findFirst()
                .map(m -> m.mime)
                .orElse(UNKNOWN.mime);
    }
}