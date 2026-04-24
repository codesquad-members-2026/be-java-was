package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Article {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createAt;

    public Article(Long id, Long userId, String content, LocalDateTime createAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.createAt = createAt;
    }

    public Article(Long userId, String content) {
        this(null, userId, content, null);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public String getCreateAt() {
        return createAt.format(formatter);
    }
}
