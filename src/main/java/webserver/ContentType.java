package webserver;

import java.util.HashMap;
import java.util.Map;

public enum ContentType {
    HTML("text/html;charset=utf-8"),
    CSS("text/css; charset=utf-8"),
    JS("text/javascript; charset=utf-8"),
    ICO("image/x-icon"),
    PNG("image/png"),
    JPG("image/jpeg"),
    SVG("image/svg+xml"),
    DEFAULT("text/plain; charset=utf-8");
    //DEFAULT("application/octet-stream"); 우선 기본은 텍스트로 보여주기

    private String contentType;

    private static final Map<String, ContentType> CONTENT_TYPE_MAP = new HashMap<>();

    static {
        for (ContentType value : ContentType.values()) {
            CONTENT_TYPE_MAP.put(value.name(), value);
        }
    }

    public static ContentType getContentType(String ext) {
        if (ext == null || ext.isEmpty()) {
            return DEFAULT;
        }

        return CONTENT_TYPE_MAP.getOrDefault(ext.toUpperCase(), DEFAULT);
    }


    ContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
