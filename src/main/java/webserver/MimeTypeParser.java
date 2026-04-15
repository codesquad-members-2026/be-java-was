package webserver;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeParser {


    // TODO : enum transition


    public enum MimeType{
        HTML("html", "text/html;charset=utf-8"),
        CSS("css","text/css;charset=utf-8"),
        JS("js",   "application/javascript;charset=utf-8"),
        ICO("ico",  "image/x-icon"),
        PNG("png",  "image/png"),
        JPG("jpg",  "image/jpeg"),
        JPEG("jpeg", "image/jpeg"),
        SVG("svg",  "image/svg+xml");

        private String contentType;
        private String fileExtension;

        private static final Map<String, MimeType> extensionMap = new HashMap<>();

        static{
            for(MimeType m : MimeType.values()){
                extensionMap.put(m.fileExtension,m);
            }
        }

        private MimeType(String fileExtension, String contentType){
            this.fileExtension = fileExtension;
            this.contentType = contentType;
        }

        public static String resolveContentType(String extension) {
            if(extension == null){
                return "application/octet-stream";
            }
            MimeType m = extensionMap.get(extension.toLowerCase());
            if(m != null){
                return m.contentType;
            }
            return "application/octet-stream";

        }

        public String getContentType(){
            return this.contentType;
        }

        public String getFileExtension(){
            return this.fileExtension;
        }
    }


    public static String extractExtension(String url) {
        int lastDotIndex = url.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < url.length() - 1) {
            return url.substring(lastDotIndex + 1);
        }
        return ""; // No extension found
    }

    public static String getContentType(String extension){
        return MimeType.resolveContentType(extension.toLowerCase());
    }

}