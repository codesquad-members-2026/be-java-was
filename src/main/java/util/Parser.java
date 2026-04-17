package util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Parser {

    // TODO: 유틸리티 클래스이므로 외부에서 new Parser()로 객체를 생성하지 못하도록 생성자를 제한해 보세요.

    public static Map<String, String> parseQueryString(String queryString) {

        if (queryString == null || queryString.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, String> map = new HashMap<>();
        String[] pairs = queryString.split("&");

        for (String pair : pairs) {
            // TODO: split("=") 대신, 값(Value)에 '='이 포함되어 있어도 안전하게 첫 번째 '='만 기준으로 나누는 방법을 고민해 보세요.
            String[] keyValue = pair.split("=");

            String key = keyValue[0].trim();
            String value = (keyValue.length > 1) ? keyValue[1].trim() : "";

            try {
                // TODO: value뿐만 아니라 key값도 URLDecoding 처리를 해주는 것이 더 완벽한 해독기가 됩니다.
                String decodedValue = URLDecoder.decode(value, "UTF-8");
                map.put(key, decodedValue);
            } catch (UnsupportedEncodingException e) {
                map.put(key, value);
            }
        }
        return map;
    }

    public static void parseCookies(String queryString, Map<String, String> cookies) {
        // TODO: 전달된 queryString이 null이거나 비어있을 때 발생할 수 있는 NullPointerException에 대비해 보세요.
        String[] pairs = queryString.split(";");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0].trim();
            String value = (keyValue.length > 1) ? keyValue[1].trim() : "";

            cookies.put(key, value);
        }
    }
}