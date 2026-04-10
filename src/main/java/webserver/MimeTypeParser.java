package webserver;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeParser {
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static{
        MIME_TYPES.put("html", "text/html;charset=utf-8");
        MIME_TYPES.put("css",  "text/css;charset=utf-8");
        MIME_TYPES.put("js",   "application/javascript;charset=utf-8");
        MIME_TYPES.put("ico",  "image/x-icon");
        MIME_TYPES.put("png",  "image/png");
        MIME_TYPES.put("jpg",  "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("svg",  "image/svg+xml");
    }

    public static String extractExtension(String url) {
        int lastDotIndex = url.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < url.length() - 1) {
            return url.substring(lastDotIndex + 1);
        }
        return ""; // No extension found
    }

    public static String getContentType(String extension){
        return MIME_TYPES.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }

}