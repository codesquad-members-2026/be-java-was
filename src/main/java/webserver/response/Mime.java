package webserver.response;

import java.util.Arrays;

public enum Mime {
    HTML("html", "text/html;charset=utf-8"),
    CSS("css", "text/css"),
    JS("js", "application/javascript"),
    ICO("ico", "image/x-icon"),
    PNG("png", "image/png"),
    SVG("svg", "image/svg+xml"),
    JPG("jpg", "image/jpeg"),
    JPEG("jpeg", "image/jpeg"),
    GIF("gif", "image/gif"),
    TXT("txt","text/plain;charset=utf-8"),
    DEFAULT("default", "application/octet-stream");

    private final String extension;
    private final String contentType;

    Mime(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }
    public String getContentType() {
        return contentType;
    }

    public static String getContentTypeThroughExtension(String extension) {
        return Arrays.stream(values())
                .filter(m -> m.getExtension().equals(extension))
                .findFirst()
                .orElse(DEFAULT)
                .getContentType();
    }
}
