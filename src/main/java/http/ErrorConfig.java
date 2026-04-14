package http;

public record ErrorConfig(
        String path,
        String statusCode
) {}