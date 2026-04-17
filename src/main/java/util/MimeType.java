package util;

public enum MimeType {

    HTML(".html", "text/html"),
    CSS(".css", "text/css"),
    JS(".js", "text/javascript"),
    SVG(".svg", "image/svg+xml"),
    PNG(".png", "image/png"),
    ICO(".ico", "image/x-icon"),
    // TODO: JSON, 폰트(woff, ttf) 등 현대 웹에서 자주 쓰이는 MIME 타입을 추가해 보세요.
    UNKNOWN("", "application/octet-stream");

    private final String extension;
    private final String mime;

    MimeType(String extension, String mime) {
        this.extension = extension;
        this.mime = mime;
    }

    public static String getMime(String path) {
        // TODO: 만약 path에 쿼리 스트링(?)이나 앵커(#)가 포함되어 있다면,
        // 확장자를 정확히 추출하기 위해 해당 부분을 먼저 잘라내는 로직이 필요합니다.
        String lowerPath = path.toLowerCase();

        return java.util.Arrays.stream(values())
                .filter(m -> !m.extension.isEmpty())
                .filter(m -> lowerPath.endsWith(m.extension))
                .findFirst()
                .map(m -> m.mime)
                .orElse(UNKNOWN.mime);
    }

    // TODO (심화): 매번 Stream을 돌리는 대신, static 블록에서 Map<String, String>을 만들어
    // 미리 캐싱해두고 get()으로 꺼내 쓰는 방식으로 리팩토링해 보세요.
}