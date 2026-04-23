package core.routing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public record RoutedInfo(String routedPath, RouteType routeType, Map<String, String> headers,
                         Map<String, String> queries, Map<String, Object> models) {

    private static final String REDIRECT_CONSTANT = "redirect:";
    private static final String QUESTION = Pattern.quote("?");
    private static final String AMPERSAND = Pattern.quote("&");
    private static final String EQUALS = Pattern.quote("=");

    public RoutedInfo {
        headers = Map.copyOf(headers);
        queries = Map.copyOf(queries);
        models = Map.copyOf(models);
    }

    public static RoutedInfo of(String rawPath, RouteType routeType,
                                Map<String, String> headers, Map<String, Object> models) {
        boolean isRedirect = RouteType.REDIRECT.equals(routeType);
        String removeRedirectPath = isRedirect ? rawPath.substring(REDIRECT_CONSTANT.length()) : rawPath;
        String[] querySplits = removeRedirectPath.split(QUESTION, 2);
        String purePath = querySplits[0];
        Map<String, String> queries = querySplits.length == 2 ? extractQueries(querySplits[1]) : new HashMap<>();

        return new RoutedInfo(purePath, routeType, headers, queries, models);
    }

    private static Map<String, String> extractQueries(String query) {
        String[] keyValues = query.split(AMPERSAND);
        Map<String, String> queries = new HashMap<>();
        Arrays.stream(keyValues).forEach(kv -> {
            String[] splits = kv.split(EQUALS, 2);
            String key = splits[0];
            String value = splits.length == 2 ? splits[1] : "";
            queries.put(key, value);
        });

        return queries;
    }
}