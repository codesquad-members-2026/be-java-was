package util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Parser {


    public static Map<String, String> parseQueryString(String queryString) {

        if (queryString == null || queryString.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, String> map = new HashMap<>();
        String[] pairs = queryString.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");

            String key = keyValue[0].trim();
            String value = (keyValue.length > 1) ? keyValue[1].trim() : "";

            try {
                String decodedValue = URLDecoder.decode(value, "UTF-8");
                map.put(key, decodedValue);
            } catch (UnsupportedEncodingException e) {
                map.put(key, value);
            }
        }
        return map;
    }

    public static void parseCookies(String queryString, Map<String, String> cookies) {

        String[] pairs = queryString.split(";");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0].trim();
            String value = (keyValue.length > 1) ? keyValue[1].trim() : "";

            cookies.put(key, value);
        }
    }
}
