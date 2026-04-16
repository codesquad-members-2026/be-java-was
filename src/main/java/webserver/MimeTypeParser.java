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
        SVG("svg",  "image/svg+xml"),
        OCTET("bin","application/octet-stream");

        private final String contentType;
        private final String fileExtension;

        private MimeType(String fileExtension, String contentType){
            this.fileExtension = fileExtension;
            this.contentType = contentType;
        }

        public static String resolveContentType(String extension) {
            if(extension == null){
                return "application/octet-stream";
            }

            MimeType retMimeType = OCTET;
            for(MimeType m : MimeType.values()){
                if(extension.equals(m.getFileExtension())){
                    retMimeType = m;
                }
            }


            return retMimeType.getContentType();

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