package model;

import java.time.LocalDateTime;

public class Article {
    private int id;
    private String title;
    private String content;
    private String authorName;
    private int authorId;
    private LocalDateTime createdAt;

    public Article(int idIdx, String title, String content, String authorName, int authorId, LocalDateTime createdAt) {
        this.id = idIdx;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.authorName = authorName;
    }

    public int getId() {
        return id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getAuthorId() {
        return authorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
